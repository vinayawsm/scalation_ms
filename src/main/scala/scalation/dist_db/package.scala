package scalation

import scala.reflect.ClassTag
import scalation.columnar_db.Relation
import scalation.columnar_db.TableObj.Row
import scalation.linalgebra.Vec

/**
  * Created by vinay on 11/12/18.
  */
package object dist_db {

    /** Boolean function that uses the value for the given column name (String)
      *  in the predicate (e.g., used by 'where' and 'filter')
      */
    type Predicate [T] = (String, T => Boolean)

    // persistence methods
    case class saveRelation (n: Seq[String])
    case class dropRelation (n: Seq[String])
    case class loadRelation (n: Seq[String])
    case class saveRelationIn (n: String)
    case class dropRelationIn (n: String)
    case class loadRelationIn (n: String)
    case class loadRelReplyIn (n: String, r: Relation)

    // Relation methods
    case class createInR (r: Relation)
    case class createFromCSV (fname: Seq[String], name: Seq[String], colname: Seq[String], key: Int, domain: String,
                              skip: Int = 0, eSep: String = ",")
    case class createFromCSVIn (fname: String, name: String, colname: Seq[String], key: Int, domain: String,
                                skip: Int, eSep: String)
    case class createReply (name: String)

    case class add (name: String, t: Row)
    case class addIn (name: String, t: Row)
    case class addReply (name: String, n: Int)
    case class materialize (name: String)
    case class materializeIn (name: String)

    case class tableGen (name: String, count: Int)
    case class tableGenIn (name: String, count: Int)

    case class show (name: Seq [String], limit: Int = 5)
    case class showIn (name: String, limit: Int)

    // def select [T : ClassTag] (cName: String, p: T => Boolean): Relation
    // rName -> resultName
    case class select [T: ClassTag] (name: Seq[String], rName: Seq[String], p: Predicate[T])
    // t -> total relations in current operation
    // uc -> (most likely) unique code
    case class selectIn [T: ClassTag] (name: String, rName: String, p: Predicate[T], t: Int, uc: String)


    case class project (name: Seq[String], rName: Seq[String], cNames: Seq[String])
    case class projectIn (name: String, rName: String, cNames: Seq[String], t: Int, uc: String)

    case class union (name: Seq[String], rName: String)
    case class unionIn (name: String, rName: String, t: Int, uc: String)
    case class unionReply (r: Relation, rName: String, t: Int, uc: String)

    case class minus (name: Seq [String], name2: Seq[String], rName: Seq[String])
    case class minusIn (name: String, name2: String, rName: String, t: Int, uc: String)

    case class product (name: Seq [String], name2: Seq[String], rName: Seq[String])
    case class productIn (name: String, name2: String, rName: String, t: Int, uc: String)

    case class join (name: Seq [String], name2: Seq[String], rName: Seq[String])
    case class joinIn (name: String, name2: String, rName: String, t: Int, uc: String)

    case class intersect (name: Seq [String], name2: Seq[String], rName: Seq[String])
    case class intersectIn (name: String, name2: String, rName: String, t: Int, uc: String)

    case class getRelation (name: Seq[String], rName: String)
    case class getRelationIn  (name: String, rName: String, t: Int, uc: String)

    case class relReply (id: String, r: Relation, rName: String, t: Int = 1)

    case class msgReply (rName: String, uc: String, t: Int, method: String)

    case class delete (name: Seq [String])
    case class deleteIn (name: String)

    case object nameAll

}
