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

    var relMap : Map[String, Relation] = Map[String, Relation]()

    // DBWorker is implementation of worker nodes.
    // This receives messages from master and performs the operations from columnar_db
    def DBWorker() : Receive = {

        case createInR (r) =>
            relMap += (r.name -> r)
            sender() ! createReply (r.name)

        case createFromCSVIn (fname: String, name: String, colname: Seq[String], key: Int, domain: String,
        skip: Int, eSep: String) =>
            val r = Relation (fname, name, colname, key, domain, skip, eSep)
            relMap += (name -> r)
            createReply (name)

        case selectIn (name, rName, p, t, uc) =>
            relMap += (rName -> relMap(name).select (p._1, p._2))
            sender() ! msgReply (rName, uc, t, name + "_select")

        case projectIn (name, rName, cNames, t, uc) =>
            relMap += (rName -> relMap(name).project(cNames : _*))
            sender() ! msgReply (rName, uc, t, name + "_project")

        case unionIn (name, rName, t, uc) =>
            sender() ! unionReply (relMap (name), rName, t, uc)

        case minusIn (name, name2, rName, t, uc) =>
            //            sender() ! relReply ("minus_" + uc + "_" + rName, r.minus(q), rName)
            relMap += (rName -> relMap(name).minus (relMap(name2)))
            sender() ! msgReply (rName, uc, t, name + "_minus")

        case productIn (name, name2, rName, t, uc) =>
            //            sender() ! relReply ("product_" + uc + "_" + rName, r.product(q), rName)
            relMap += (rName -> relMap(name).product (relMap(name2)))
            sender() ! msgReply (rName, uc, t, name + "_product")

        case joinIn (name, name2, rName, t, uc) =>
            //            sender() ! relReply ("product_" + uc + "_" + rName, r.join(q), rName)
            relMap += (rName -> relMap(name).join (relMap(name2)).asInstanceOf [Relation])
            sender() ! msgReply (rName, uc, t, name + "_join")

        case intersectIn (name, name2, rName, t, uc) =>
            //            sender() ! relReply ("product_" + uc + "_" + rName, r.intersect(q), rName)
            relMap += (rName -> relMap(name).intersect (relMap(name2)))
            sender() ! msgReply (rName, uc, t, name + "_intersect")

        case showIn (name, limit) =>
            relMap(name).show(limit)

        case getRelationIn (name, rName, t, uc) =>
            relReply (uc, relMap (name), rName, t)

        case deleteIn (name) =>
            relMap = relMap - name

        case nameAll =>
            println(self.path)
            println(relMap.keys)

    }

    override def receive: Receive = DBWorker()

}
