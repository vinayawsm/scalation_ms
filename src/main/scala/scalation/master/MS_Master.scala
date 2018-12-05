package scalation.master

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool
import akka.persistence.PersistentActor

import scala.collection.mutable.ArrayBuffer
import scalation.{analytics, preprocessing}
import scalation.analytics._
import scalation.columnar_db._
import scalation.dist_db._
import scalation.linalgebra.Vec
import scalation.preprocessing.PreProcessingMaster

object RelationPersistence {

    private var mem: Map[ String, Relation ] = Map[ String, Relation ]()

    case class m_saveRelation (n: String, r: Relation)
    case class m_dropRelation (n: String)
    case class p_saveRelation (n: String, r: Relation)
    case class p_dropRelation (n: String)
    case class p_getRelation (n: String)

}

class RelationPersistence extends PersistentActor {

    override def persistenceId: String = "Relation_Persistence"

    import RelationPersistence._

    override def receiveRecover: Receive = {
        case m_saveRelation (n, r) =>
            mem = mem + (n -> r)
        case m_dropRelation (n) =>
            mem = mem - n
    }

    override def receiveCommand: Receive = {
        case p_saveRelation (n, r) =>
            persist (m_saveRelation (n, r)) {
                savingRelation => mem = mem + (n -> r)
            }
        case p_dropRelation (n) =>
            persist (m_dropRelation (n)) {
                savingRelation => mem = mem - n
            }
        case p_getRelation (n) =>
            sender() ! getRelReply (n, mem(n))
    }

}

class MS_Master extends MasterUtil with Actor
{
    import RelationPersistence._

    val db: ActorRef = context.actorOf (RoundRobinPool (db_worker_count).props(Props[RelDBWorker]), "db_router")
    val pp: ActorRef = context.actorOf (Props[PreProcessingMaster], "router")
    val an: ActorRef = context.actorOf (RoundRobinPool (an_worker_count).props(Props[AnalyticsMaster]), "an_router")

    val pactor: ActorRef = context.actorOf (Props[RelationPersistence], "persistence_actor")

    def messageHandler(): Receive =
    {
        // databases

        // persistence methods
        case saveRelation (n) =>
            pactor ! p_saveRelation (n, tableMap(n))
        case dropRelation (n) =>
            pactor ! p_dropRelation (n)
        case getRelation (n) =>
            updating += (n -> true)
            pactor ! p_getRelation (n)
        case getRelReply (n, r) =>
            if (!tableMap.exists(_._1 == n)) tableMap += (n -> r)
            updating += (n -> false)

        // create new relation
        case create (name, colname, key, domain) =>
            updating += (name -> true)
            if (!tableMap.exists(_._1 == name)) {
                val r = Relation (name, colname, Seq(), key, domain)
                tableMap += (name -> r)
            }
            updating += (name -> false)

        // reply for create message. recieves name of relation and status (-1 => already exists, else => created)
        case createReply (name: String, n: Int) =>
            if (n == -1) println ("Table " + name + " already exists")
            else println ("Table " + name + " created")

        // add new row to the table
        case add (name, t) =>
            updating += (name -> true)
            if (tableMap.exists(_._1 == name)) tableMap(name).add(t)
            updating += (name -> false)

        // reply for add message. recieves name of relation and status (-1 => table doesn't exists)
        case addReply (name: String, n: Int) =>
            if (n == -1) println ("Table " + name + " doesn't exists")
            else println ("Row added to table " + name)

        // materialize the table. must do after addition of table rows
        case materialize (name) =>
            updating += (name -> true)
            tableMap(name).materialize()
            updating += (name -> false)

        // creates same "random" tables in all the worker nodes (this is for worker nodes to have entire tables)
        case tableGen (name, count) =>
            updating += (name -> true)
            TableGen.popTable (tableMap(name), count)
            updating += (name -> false)

        case select (name, p, rName) =>
            //            router ! Broadcast (selectIn (ri.nextInt(randomSeed), name, p))
            updCheck ("select", name)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! selectIn (rid, subRelation (tableMap(name), i * perN,  math.min(rc, perN * (i + 1))), p, rName)

        // performed on master
        case project (name, cNames, rName) =>
            updCheck ("project", name)
            updating += (rName -> true)
            var pr = tableMap(name).project(cNames: _*)
            pr.name = rName
            tableMap += (rName -> pr)
            updating += (rName -> false)

        // performed on master
        case union (name, name2, rName) =>
            updCheck ("union", name)
            updCheck ("union", name2)
            updating += (rName -> true)
            var ur = tableMap(name).union(tableMap(name2))
            ur.name = rName
            tableMap += (rName -> ur)
            updating += (rName -> false)

        case minus (name, name2, rName) =>
            updCheck ("minus", name)
            updCheck ("minus", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! minusIn (rid, subRelation (tableMap(name), i * perN,  math.min (rc, perN * (i + 1))),
                    tableMap(name2), rName)

        case product (name, name2, rName) =>
            updCheck ("product", name)
            updCheck ("product", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! productIn (rid, subRelation (tableMap(name), i * perN,  math.min (rc, perN * (i + 1))),
                    tableMap(name2), rName)

        case join (name, name2, rName) =>
            updCheck ("join", name)
            updCheck ("join", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! joinIn (rid, subRelation (tableMap(name), i * perN,  math.min (rc, perN * (i + 1))),
                    tableMap(name2), rName)

        case intersect (name, name2, rName) =>
            updCheck ("intersect", name)
            updCheck ("intersect", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! intersectIn (rid, subRelation (tableMap(name), i * perN,  math.min (rc, perN * (i + 1))),
                    tableMap(name2), rName)

        // show the table
        // if you are getting updating warning, you may add sleep(time) before calling show()
        case show (name) =>
            updCheck ("show", name)
            tableMap (name).show()

        case delete (name) =>
            updCheck ("delete", name)
            if (tableMap.exists (_._1 == name)) tableMap -= name


        // preprocessing
        case project (r, cNames, rName) =>
            pp ! preprocessing.project (r, cNames, rName)

        case mapToInt (v, vName) =>
            pp ! preprocessing.mapToInt (v, vName)

        case replaceMissingValues (r, mCol, mVal, fVal, frac, rName) =>
            pp ! preprocessing.replaceMissingValues (r, mCol, mVal, fVal, frac, rName)

        case replaceMissingStrings (r, mCol, mVal, fVal, frac, rName) =>
            pp ! preprocessing.replaceMissingStrings (r, mCol, mVal, fVal, frac, rName)

        case rmOutliers (method, c, args, vName) =>
            pp ! preprocessing.rmOutliers (method, c, args, vName)

        case impute (method, c, args, vName) =>
            pp ! preprocessing.impute (method, c, args, vName)

        // to matrix
        case toMatriD (r, colPos, kind, mName)  => pp ! preprocessing.toMatriD (r, colPos, kind, mName)
        case toMatriI (r, colPos, kind, mName)  => pp ! preprocessing.toMatriI (r, colPos, kind, mName)
        case toMatriI2 (r, colPos, kind, mName) => pp ! preprocessing.toMatriI2 (r, colPos, kind, mName)

        // to matrix + vector
        case toMatriDD (r, colPos, colPosV, kind, mName, vName) =>
            updating +=  (vName -> true)
            updating +=  (vName -> true)
            pp ! preprocessing.toMatriDD (r, colPos, colPosV, kind, mName, vName)
        case toMatriDI (r, colPos, colPosV, kind, mName, vName) =>
            updating +=  (vName -> true)
            updating +=  (vName -> true)
            pp ! preprocessing.toMatriDD (r, colPos, colPosV, kind, mName, vName)
        case toMatriII (r, colPos, colPosV, kind, mName, vName) =>
            updating +=  (vName -> true)
            updating +=  (vName -> true)
            pp ! preprocessing.toMatriDD (r, colPos, colPosV, kind, mName, vName)

        // to vector
        case toVectorD (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorD (r, colPos, vName)
        case toVectorI (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorI (r, colPos, vName)
        case toVectorS (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorS (r, colPos, vName)
        case toVectorD2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorD2 (r, colPos, vName)
        case toVectorI2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorI2 (r, colPos, vName)
        case toVectorS2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toVectorS2 (r, colPos, vName)
        case toRleVectorD (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorD (r, colPos, vName)
        case toRleVectorI (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorI (r, colPos, vName)
        case toRleVectorS (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorS (r, colPos, vName)
        case toRleVectorD2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorD2 (r, colPos, vName)
        case toRleVectorI2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorI2 (r, colPos, vName)
        case toRleVectorS2 (r, colPos, vName)      =>
            updating +=  (vName -> true)
            pp ! preprocessing.toRleVectorS2 (r, colPos, vName)

        // analytics

        case expSmoothing (method, t, x, l, m, validateSteps, steps) =>
            an ! analytics.expSmoothing (method, t, x, l, m, validateSteps, steps)

        case arima (method, t, y, d, p, q, transBack, steps) =>
            an ! analytics.arima (method, t, y, d, p, q, transBack, steps)

        case sarima (method, t, y, d, dd, period, xxreg, p, q ,steps, xxreg_f) =>
            an ! analytics.sarima (method, t, y, d, dd, period, xxreg, p, q ,steps, xxreg_f)


        // result methods

        case relReply2 (id, r, rName) =>
            // add elements to retTableMap -> do union of all the results -> (remove the entry from retTableMap)
            if (retTableMap.exists (_._1 == id))
                retTableMap (id) += r
            else
                retTableMap += (id -> ArrayBuffer (r))
            if (retTableMap (id).size == db_worker_count) {
                var r: Relation = retTableMap (id)(0)
                for (i <- 1 until db_worker_count)
                    r = r union retTableMap (id)(i)
                r.name = rName
                tableMap += (rName -> r)
                retTableMap -= id
                updating += (rName -> false)
            }

        case recvRelation (rName, r) =>
            tableMap += (rName -> r)
            updating += (rName -> false)

        case recvMatrixD (mName, m) =>
            matDMap += (mName -> m)
            updating += (mName -> false)

        case recvMatrixI (mName, m) =>
            matIMap += (mName -> m)
            updating += (mName -> false)

        case recvMatrixS (mName, m) =>
            matSMap += (mName -> m)
            updating += (mName -> false)

        case recvVectoD (vName, v) =>
            vecDMap += (vName -> v)
            updating += (vName -> false)

        case recvVectoI (vName, v) =>
            vecIMap += (vName -> v)
            updating += (vName -> false)

        case recvVectoS (vName, v) =>
            vecSMap += (vName -> v)
            updating += (vName -> false)


    }

    override def receive: Receive = messageHandler()

    def preComp (name: String, rName: String): (Int, Int, Int) =
    {
        updating += (rName -> true)
        val rid = ri.nextInt (randomSeed)
        val rc = tableMap(name).rows
        val perN = math.ceil ((rc * 1.0) / db_worker_count).toInt
        (rid, rc, perN)
    }

    def updCheck (f: String, name: String) =
    {
        if (updating(name)) {
            println (f + ": Table " + name + " is not updated yet")
            println ("Possible solution is to use Thread.sleep(n) before this query")
        }
    }

    def subRelation (r: Relation, se: Int, le: Int): Relation =
    {
        val newCol = (for (j <- r.col.indices) yield Vec.select (r.col(j), Seq.range(se, le))).toVector
        new Relation (r.name, r.colName, newCol, r.key, r.domain)
    }
}
