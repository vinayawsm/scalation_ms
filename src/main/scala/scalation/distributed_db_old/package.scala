package scalation

import scala.reflect.ClassTag
import scalation.columnar_db.Relation
import scalation.columnar_db.TableObj.Row
import scalation.linalgebra.Vec

/**
  * Created by vinay on 10/24/18.
  */
package object distributed_db {

    type Predicate [T] = (String, T => Boolean)

    case class create (name: String, colname: Seq[String], key: Int, domain: String)
    case class createIn (name: String, colname: Seq[String], key: Int, domain: String)
    case class createReply (name: String, n: Int)

    case class add (name: String, t: Row)
    case class addIn (name: String, t: Row)
    case class addReply (name: String, n: Int)
    case class materialize (name: String)
    case class materializeIn (name: String)

    case class show (name: String)
    case class showIn (uc: Int, name: String)

    // def select [T : ClassTag] (cName: String, p: T => Boolean): Relation
    case class select [T: ClassTag] (name: String, p: Predicate[T])
    case class selectIn [T: ClassTag] (uc: Int, name: String, p: Predicate[T])

    // def project (cName: String*): Relation
    case class project (name: String, cNames: Seq[String])
    case class projectIn (uc: Int, name: String, cNames: Seq[String])

    // def union (r2: Table): Relation
    case class union (r: String, q: String)
    case class unionIn (uc: Int, r: String, q: String)

    // def minus (r2: Table): Relation
    case class minus (r: String, q: String)
    case class minusIn (uc: Int, r: String, q: String)

    // def product (r2: Table): Relation
    case class product (r: String, q: String)
    case class productIn (uc: Int, r: String, q: String)

    // def join (r2: Table): Table
    case class join (r: String, q: String)
    case class joinIn (uc: Int, r: String, q: String)

    // def intersect (_r2: Table): Relation
    case class intersect (r: String, q: String)
    case class intersectIn (uc: Int, r: String, q: String)

    case class relReply (id: String, r: Relation)

    case object nameAll

}