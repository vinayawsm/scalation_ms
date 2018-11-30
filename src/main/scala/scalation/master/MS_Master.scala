package scalation.master

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool

import scala.collection.mutable.ArrayBuffer
import scalation.columnar_db.{Relation, TableGen}
import scalation.dist_db._
import scalation.linalgebra.Vec

class MS_Master extends MasterUtil with Actor
{

    val db: ActorRef = context.actorOf (RoundRobinPool (db_worker_count).props(Props[RelDBWorker]), "router")

    def messageHandler(): Receive =
    {
        // create new relation
        case create (name, colname, key, domain) =>
            updating += (name -> true)
            if (!tableMap.exists(_._1 == name)) {
                val r = Relation (name, colname, Seq(), key, domain)
                rSeq += r
                tableMap += (name -> (rSeq.size - 1))
            }
            updating += (name -> false)

        // reply for create message. recieves name of relation and status (-1 => already exists, else => created)
        case createReply (name: String, n: Int) =>
            if (n == -1) println ("Table " + name + " already exists")
            else println ("Table " + name + " created")

        // add new row to the table
        case add (name, t) =>
            updating += (name -> true)
            if (tableMap.exists(_._1 == name)) {
                rSeq(tableMap(name)).add(t)
            }
            updating += (name -> false)

        // reply for add message. recieves name of relation and status (-1 => table doesn't exists)
        case addReply (name: String, n: Int) =>
            if (n == -1) println ("Table " + name + " doesn't exists")
            else println ("Row added to table " + name)

        // materialize the table. must do after addition of table rows
        case materialize (name) =>
            updating += (name -> true)
            rSeq(tableMap(name)).materialize()
            updating += (name -> false)

        // creates same "random" tables in all the worker nodes (this is for worker nodes to have entire tables)
        case tableGen (name, count) =>
            updating += (name -> true)
            TableGen.popTable (rSeq(tableMap(name)), count)
            updating += (name -> false)

        case select (name, p, rName) =>
            //            router ! Broadcast (selectIn (ri.nextInt(randomSeed), name, p))
            relCheck ("select", name)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! selectIn (rid, subRelation (rSeq (tableMap(name)), i * perN,  math.min(rc, perN * (i + 1))), p, rName)

        // performed on master
        case project (name, cNames, rName) =>
            relCheck ("project", name)
            updating += (rName -> true)
            var pr = rSeq(tableMap(name)).project(cNames: _*)
            pr.name = rName
            rSeq += pr
            tableMap += (rName -> (rSeq.size - 1))
            updating += (rName -> false)

        // performed on master
        case union (name, name2, rName) =>
            relCheck ("union", name)
            relCheck ("union", name2)
            updating += (rName -> true)
            var ur = rSeq(tableMap(name)).union(rSeq(tableMap(name2)))
            ur.name = rName
            rSeq += ur
            tableMap += (rName -> (rSeq.size - 1))
            updating += (rName -> false)

        case minus (name, name2, rName) =>
            relCheck ("minus", name)
            relCheck ("minus", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! minusIn (rid, subRelation (rSeq (tableMap(name)), i * perN,  math.min (rc, perN * (i + 1))),
                    rSeq (tableMap(name2)), rName)

        case product (name, name2, rName) =>
            relCheck ("product", name)
            relCheck ("product", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! productIn (rid, subRelation (rSeq (tableMap(name)), i * perN,  math.min (rc, perN * (i + 1))),
                    rSeq (tableMap(name2)), rName)

        case join (name, name2, rName) =>
            relCheck ("join", name)
            relCheck ("join", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! joinIn (rid, subRelation (rSeq (tableMap(name)), i * perN,  math.min (rc, perN * (i + 1))),
                    rSeq (tableMap(name2)), rName)

        case intersect (name, name2, rName) =>
            relCheck ("intersect", name)
            relCheck ("intersect", name2)
            val (rid, rc, perN) = preComp (name, rName)
            for (i <- 0 until db_worker_count)
                db ! intersectIn (rid, subRelation (rSeq (tableMap(name)), i * perN,  math.min (rc, perN * (i + 1))),
                    rSeq (tableMap(name2)), rName)

        // show the table
        // if you are getting updating warning, you may add sleep(time) before calling show()
        case show (name) =>
            relCheck ("show", name)
            rSeq (tableMap (name)).show()

        case relReply (id, r) =>
            // add elements to retTableMap -> do union of all the results -> (remove the entry from retTableMap)
            if (retTableMap.exists (_._1 == id))
                retTableMap (id) += r
            else
                retTableMap += (id -> ArrayBuffer(r))
            if (retTableMap (id).size == db_worker_count) {
                var r: Relation = retTableMap (id)(0)
                for (i <- 1 until db_worker_count)
                    r = r union retTableMap (id)(i)
                r.show ()
                retTableMap -= id
            }

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
                rSeq += r
                tableMap += (rName -> (rSeq.size - 1))
                retTableMap -= id
                updating += (rName -> false)
            }

        case delete (name) =>
            relCheck ("delete", name)
            if (tableMap.exists (_._1 == name)) {
                val ind = tableMap (name)
                rSeq.remove (ind)
                tableMap -= name
                tableMap = tableMap ++ tableMap.filter(_._2 > ind).mapValues(_ - 1)
            }
    }

    override def receive: Receive = messageHandler()

    def preComp (name: String, rName: String): (Int, Int, Int) =
    {
        updating += (rName -> true)
        val rid = ri.nextInt (randomSeed)
        val rc = rSeq (tableMap(name)).rows
        val perN = math.ceil ((rc * 1.0) / db_worker_count).toInt
        (rid, rc, perN)
    }

    def relCheck (f: String, name: String) =
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
