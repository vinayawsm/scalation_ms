package scalation.master

import scala.collection.mutable.ArrayBuffer
import scalation.columnar_db.Relation


trait MasterUtil {
    val db_worker_count = 4
    val randomSeed = 1000000

    // Sequence of relations
    var rSeq: ArrayBuffer[Relation] = ArrayBuffer()

    // map of tables in database
    // params: name, index of "name" relation in rSeq
    var tableMap : Map[String, Int] = Map[String, Int]()

    // map of relations that are returned from workers
    // params: name, Array of relations returned from worker nodes
    var retTableMap : Map[String, scala.collection.mutable.ArrayBuffer[Relation]] = Map[String, ArrayBuffer[Relation]]()

    var updating: Map[String, Boolean] = Map[String, Boolean]()

    val ri = scala.util.Random
}
