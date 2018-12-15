package scalation.master

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool

import scala.collection.mutable.ArrayBuffer
import scalation.{analytics, preprocessing}
import scalation.analytics._
import scalation.columnar_db._
import scalation.dist_db._
import scalation.linalgebra.Vec
import scalation.preprocessing.PreProcessingMaster


class MS_Master extends MasterUtil with Actor
{

    val db = new Array[ActorRef](db_worker_count)
    for (i <- 0 until db_worker_count)
        db (i) = context.actorOf (Props[RelDBWorker], s"db_worker$i")
    val pp: ActorRef = context.actorOf (Props[PreProcessingMaster], "router")
    val an: ActorRef = context.actorOf(Props[AnalyticsWorker], "root")

    var relNodeMap : Map [String, Int] = Map [String, Int] ()
    var msgReplyMap: Map [String, Int] = Map [String, Int] () // unique code -> counter

    def messageHandler(): Receive =
    {
        // databases

        // persistence methods
        case saveRelation (name) =>
            for (i <- 0 until name.size)
                db (relNodeMap (name (i))) ! saveRelationIn (name(i))
        case dropRelation (name) =>
            for (i <- 0 until name.size)
                db (relNodeMap (name (i))) ! dropRelationIn (name(i))
        case loadRelation (name) =>
            for (i <- 0 until name.size)
                db (relNodeMap (name (i))) ! loadRelationIn (name(i))

        // Takes partitions of data from csv files and creates relations in sequential order
        // (nth .csv file will be in n%numOfRoutees node)
        case createFromCSV (fname, name, colname, key, domain, skip, eSep) =>
            for (i <- 0 until fname.size) {
                updating += (name (i) -> true)
                db(i % db_worker_count) ! createFromCSVIn (fname(i), name(i), colname, key, domain, skip, eSep)
                relNodeMap += (name (i) -> i % db_worker_count)
            }

        // reply for create message. recieves name of relation and status (-1 => already exists, else => created)
        case createReply (name) =>
            updating += (name -> false)

        case select (name, rName, p) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("select", rName (i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! selectIn (name (i), rName (i), p, name.size, "select_" + randi)
            }

        case project (name, rName, cNames) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("project", rName(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! projectIn (name (i), rName (i), cNames, name.size, "project_" + randi)
            }

        // done on master
        case union (name, rName) =>
            val randi = ri.nextInt (randomSeed)
            updating += (rName -> true)
            for (i <- 0 until name.size) {
                updCheck ("union", name (i))
                db (relNodeMap (name(i))) ! unionIn (name (i), rName, name.size, "union_" + randi)
            }

        case unionReply (r, rName, t, uc) =>
            if (retTableMap.exists (_._1 == uc))
                retTableMap (uc) += r
            else
                retTableMap += (uc -> ArrayBuffer (r))
            if (retTableMap (uc).size == t) {
                var r: Relation = retTableMap (uc)(0)
                for (i <- 1 until t)
                    r = r union retTableMap (uc)(i)
                r.name = rName
                val randi = ri.nextInt (randomSeed)
                db (randi % db_worker_count) ! createInR (r)
                relNodeMap += (rName -> randi % db_worker_count)
                updating += (rName -> false)
            }

        case minus (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("minus", name(i))
                updCheck ("minus", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! minusIn (name (i), name2 (i), rName (i), name.size, "minus_" + randi)
            }

        case product (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("product", name(i))
                updCheck ("product", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! productIn (name (i), name2 (i), rName (i), name.size, "product_" + randi)
            }

        case join (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("join", name(i))
                updCheck ("join", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! joinIn (name (i), name2 (i), rName (i), name.size, "join_" + randi)
            }

        case intersect (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("intersect", name(i))
                updCheck ("intersect", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! intersectIn (name (i), name2 (i), rName (i), name.size, "intersect_" + randi)
            }

        // show the table
        // if you are getting updating warning, you may add sleep(time) before calling show()
        case show (name, limit) =>
            for (i <- 0 until name.size) {
                db (relNodeMap (name(i))) ! showIn (name (i), limit)
            }

        case getRelation (name, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                updCheck ("getRelation", name (i))
                updating += (rName -> true)
                db (relNodeMap (name(i))) ! getRelationIn (name(i), rName, name.size, "getRelation_" + randi)
            }

        case delete (name) =>
            for (i <- 0 until name.size) {
                updCheck ("delete", name (i))
                db (relNodeMap (name(i))) ! deleteIn (name (i))
                relNodeMap = relNodeMap - name (i)
            }


        // preprocessing
        case pp_project (r, cNames, rName) =>
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

        /////////////////////////////////////// analytics
        // ClassifierInt
        case ClassifierInt (model, rName) =>
            an ! ClassifierIntIn (model, rName)

        case ClassifierInt_classify (name, z, xx, rName) =>
            an ! ClassifierIntIn_classify (name, z, xx, rName)

        case ClassifierInt_test (name, itest, xx, yy, rName) =>
            an ! ClassifierIntIn_test (name, itest, xx, yy, rName)

        case ClassifierInt_featureSelection (name, tol) =>
            an ! ClassifierIntIn_featureSelection (name, tol)

        case ClassifierInt_calcCorrelation (name, rName) =>
            an ! ClassifierIntIn_calcCorrelation (name, rName)

        case ClassifierInt_calcCorrelation2 (name, zrg, xrg, rName) =>
            an ! ClassifierIntIn_calcCorrelation2 (name, zrg, xrg, rName)

        // ClassifierReal
        case ClassifierReal (model, rName) =>
            an ! ClassifierRealIn (model, rName)

        case ClassifierReal_classify (name, z, xx, rName) =>
            an ! ClassifierRealIn_classify (name, z, xx, rName)

        case ClassifierReal_test (name, itest, xx, yy, rName) =>
            an ! ClassifierRealIn_test (name, itest, xx, yy, rName)

        case ClassifierReal_featureSelection (name, tol) =>
            an ! ClassifierRealIn_featureSelection (name, tol)

        case ClassifierReal_calcCorrelation (name, rName) =>
            an ! ClassifierRealIn_calcCorrelation (name, rName)

        case ClassifierReal_calcCorrelation2 (name, zrg, xrg, rName) =>
            an ! ClassifierRealIn_calcCorrelation2 (name, zrg, xrg, rName)

        // PredictorVec
        case PredictorVec_m (model, rName) =>
            an ! PredictorVecIn_m (model, rName)

        case PredictorVec_train (name, yy, rName) =>
            an ! PredictorVecIn_train (name, yy, rName)

        case PredictorVec_predict (name, d, v, rName) =>
            an ! PredictorVecIn_predict (name, d, v, rName)

        case PredictorVec_crossValidate (name, algor, k, rando, rName) =>
            an ! PredictorVecIn_crossValidate (name, algor, k, rando, rName)

        // PredictorMat
        case PredictorMat_m (model, rName) =>
            an ! PredictorMatIn_m (model, rName)

        case PredictorMat_train (name, yy, rName) =>
            an ! PredictorMatIn_train (name, yy, rName)

        case PredictorMat_predict (name, v, z, rName) =>
            an ! PredictorMatIn_predict (name, v, z, rName)

        case PredictorMat_crossValidate (name, algor, k, rando, rName) =>
            an ! PredictorMatIn_crossValidate (name, algor, k, rando, rName)

        // result methods

        case msgReply (rName, uc, t, m) =>
            if (msgReplyMap.exists (_._1 == uc))
                msgReplyMap += (uc -> (msgReplyMap(uc) + 1))
            else
                msgReplyMap += (uc -> 1)
            updating += (rName -> false)
            if (msgReplyMap (uc) == t) {
                println(m + " opeartion done")
                msgReplyMap = msgReplyMap - uc
            }


        case relReply (uc, r, rName, t) =>
            // add elements to retTableMap -> do union of all the results -> (remove the entry from retTableMap)
            if (retTableMap.exists (_._1 == uc))
                retTableMap (uc) += r
            else
                retTableMap += (uc -> ArrayBuffer (r))
            if (retTableMap (uc).size == t) {
                var r: Relation = retTableMap (uc)(0)
                for (i <- 1 until t)
                    r = r union retTableMap (uc)(i)
                r.name = rName
                val n = 20
                println("printing first " + n + " lines of " + rName)
                r.show (n)
                retTableMap = retTableMap - uc
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

        case printPaths() =>
            println("Master: ", self.path.address.host)
            println("DB: ", db(0).path.address.host)
            println("PP: " + pp.path)
            println("PP_host: " + pp.path.address.hostPort)
            println("AN: " + an.path)

        case nameAll =>
            println("Tables list: ")
            tableMap.foreach (n => println(n._1))
            println("\n")
            println("Matrices list: ")
            matDMap.foreach (n => println(n._1 + ", MatrixD"))
            matIMap.foreach (n => println(n._1 + ", MatrixI"))
            matSMap.foreach (n => println(n._1 + ", MatrixS"))
            println("\n")
            println("Vectors list: ")
            vecDMap.foreach (n => println(n._1 + ", VectoD"))
            vecIMap.foreach (n => println(n._1 + ", VectoI"))
            vecSMap.foreach (n => println(n._1 + ", VectoS"))
            println("\n")


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

/*//> runMain scalation.master.MS_MasterTest0
object MS_MasterTest0 extends App
{
    val actorSystem = ActorSystem("RelationDBMasterTest")
    val actor = actorSystem.actorOf(Props[MS_Master], "root")

    actor ! create("R1", Seq("Name", "Age", "Weight"), 0, "SID")

    actor ! tableGen("R1", 20)
    actor ! show("R1")
    actor ! saveRelation("R1")

    Thread.sleep(10000)
    actorSystem.terminate()
}

object MS_MasterTest0_1 extends App
{
    val actorSystem = ActorSystem("RelationDBMasterTest")
    val actor = actorSystem.actorOf(Props[MS_Master], "root")

    actor ! nameAll
    actor ! getRelation ("R1")
    Thread.sleep(2000)
    actor ! nameAll

    actor ! show ("R1")

    Thread.sleep(10000)
    actorSystem.terminate()
}

object MS_MasterTest1 extends App
{
    val actorSystem = ActorSystem ("MS_MasterTest1")
    val actor = actorSystem.actorOf (Props[MS_Master], "root")

    actor ! createFromCSV ("data/files/5_N_HV_1108436.csv", "table1", Seq("Time", "A", "Vehicles", "B", "Speed") , 0, "TIIDD")
    actor ! show("table1", 5)


    Thread.sleep(10000)
    actorSystem.terminate()
}*/

object MS_MasterTest2 extends App
{

    val actorSystem = ActorSystem ("MS_MasterTest1")
    val actor = actorSystem.actorOf (Props[MS_Master], "root")

    println(actor.path)

    actor ! printPaths()

    Thread.sleep(1000)
    actorSystem.terminate()
}