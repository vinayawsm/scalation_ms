package scalation

import scala.reflect.ClassTag
import scalation.columnar_db._
import scalation.linalgebra._

package object preprocessing {

    // def project (cName: String*): Relation
    case class project (r: Relation, cNames: Seq[String])

    // def mapToInt (s: VectoS): (VectoI, BiMap [StrNum, Int])
    case class mapToInt (name: VectoS)

    // def replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    case class replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
                                                           funcVal: Imputation = ImputeMean, fraction: Double = 0.2)

    // def replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    case class replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)

    // def rmOutliers (c: Vec, args: Double*)
    // method can be - 'DistanceOutlier', 'QuantileOutlier', 'QuartileOutlier'
    case class rmOutliers (method: String, c: Vec, args: Double*)
}