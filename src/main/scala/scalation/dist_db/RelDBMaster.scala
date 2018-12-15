package scalation.dist_db

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scalation.columnar_db.TableObj.ucount
import scalation.columnar_db.{Relation, TableGen}
import scalation.dist_db.RelDBMasterTest4.actor
import scalation.linalgebra.Vec

/**
  * Created by vinay on 10/10/18.
  */

class RelDBMaster extends DistUtil with Actor
{

    // creates a router with `numOfRoutees` routees
    // val router: ActorRef = context.actorOf (RoundRobinPool (numOfRoutees).props(Props[RelDBWorker]), "router")

    val db = new Array[ActorRef](numOfRoutees)
    for (i <- 0 until numOfRoutees)
        db (i) = context.actorOf (Props[RelDBWorker], s"db_worker$i")

    var relNodeMap : Map [String, Int] = Map [String, Int] ()
    var msgReplyMap: Map [String, Int] = Map [String, Int] () // unique code -> counter

    // DBHandler handles all the DB messages on master side
    def DBHandler (): Receive =
    {

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
                db(i % numOfRoutees) ! createFromCSVIn (fname(i), name(i), colname, key, domain, skip, eSep)
                relNodeMap += (name (i) -> i % numOfRoutees)
            }

        // reply for create message. recieves name of relation and status (-1 => already exists, else => created)
        case createReply (name) =>
            updating += (name -> false)

        case select (name, rName, p) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("select", rName (i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! selectIn (name (i), rName (i), p, name.size, "select_" + randi)
            }

        case project (name, rName, cNames) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("project", rName(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! projectIn (name (i), rName (i), cNames, name.size, "project_" + randi)
            }

        // done on master
        case union (name, rName) =>
            val randi = ri.nextInt (randomSeed)
            updating += (rName -> true)
            for (i <- 0 until name.size) {
                relCheck ("union", name (i))
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
                db (randi % numOfRoutees) ! createInR (r)
                relNodeMap += (rName -> randi % numOfRoutees)
                updating += (rName -> false)
            }

        case minus (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("minus", name(i))
                relCheck ("minus", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! minusIn (name (i), name2 (i), rName (i), name.size, "minus_" + randi)
            }

        case product (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("product", name(i))
                relCheck ("product", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! productIn (name (i), name2 (i), rName (i), name.size, "product_" + randi)
            }

        case join (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("join", name(i))
                relCheck ("join", name2(i))
                updating += (rName (i) -> true)
                db (relNodeMap (name (i))) ! joinIn (name (i), name2 (i), rName (i), name.size, "join_" + randi)
            }

        case intersect (name, name2, rName) =>
            val randi = ri.nextInt (randomSeed)
            for (i <- 0 until name.size) {
                relCheck ("intersect", name(i))
                relCheck ("intersect", name2(i))
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
                relCheck ("getRelation", name (i))
                updating += (rName -> true)
                db (relNodeMap (name(i))) ! getRelationIn (name(i), rName, name.size, "getRelation_" + randi)
            }

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

        case delete (name) =>
            for (i <- 0 until name.size) {
                relCheck ("delete", name (i))
                db (relNodeMap (name(i))) ! deleteIn (name (i))
                relNodeMap = relNodeMap - name (i)
            }


        case nameAll =>
            for(i <- 0 until db.length)
                db(i) ! nameAll

    }

    override def receive: Receive = DBHandler()

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
