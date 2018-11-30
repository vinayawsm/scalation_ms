package scalation.dist_db

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scalation.columnar_db.{Relation, TableGen}

/**
  * Created by vinay on 10/14/18.
  */


class RelDBWorker extends Actor {

    //    val routee: ActorRef = context.actorOf(Props[RelDBWorker], "routee")

    // DBWorker is implementation of worker nodes.
    // This receives messages from master and performs the operations from columnar_db
    def DBWorker() : Receive = {

        case selectIn (uc, r, p, rName) =>
            sender() ! relReply2 ("select_" + uc + "_" + rName, r.select(p._1, p._2), rName)

        case minusIn (uc, r, q, rName) =>
            sender() ! relReply2 ("minus_" + uc + "_" + rName, r.minus(q), rName)

        case productIn (uc, r, q, rName) =>
            sender() ! relReply2 ("product_" + uc + "_" + rName, r.product(q), rName)

        case joinIn (uc, r, q, rName) =>
            sender() ! relReply2 ("product_" + uc + "_" + rName, r.product(q), rName)

        case intersectIn (uc, r, q, rName) =>
            sender() ! relReply2 ("product_" + uc + "_" + rName, r.intersect(q), rName)

    }

    override def receive: Receive = DBWorker()

}
