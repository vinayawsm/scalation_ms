package scalation

import scala.reflect.ClassTag
import scalation.columnar_db._
import scalation.linalgebra.MatrixKind.MatrixKind
import scalation.linalgebra._

package object preprocessing {

    // def project (cName: String*): Relation
    case class project (r: Relation, cNames: Seq[String])

    // def mapToInt (s: VectoS): (VectoI, BiMap [StrNum, Int])
    case class mapToInt (v: VectoS)

    // def replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    // return the table xy to sender after computation
    case class replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
                                                           funcVal: Imputation = ImputeMean, fraction: Double = 0.2)

    // def replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    // return the table xy to sender after computation
    case class replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)

    // def rmOutliers (c: Vec, args: Double*)
    // method can be - 'DistanceOutlier', 'QuantileOutlier', 'QuartileXOutlier'
    case class rmOutliers (method: String, c: Vec, args: Seq[Double])

    // def impute (c: Vec, args: Int*): Any
    // method can be - 'Interpolate', 'ImputeMean', 'ImputeNormal', 'ImputeMovingAverage'
    case class impute (method: String, c: Vec, args: Seq[Int])

    // def toMatriD (colPos: Seq [Int], kind: MatrixKind = DENSE): MatriD
    case class toMatriD (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE)

    case class toMatriI (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE)

    case class toMatriI2 (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE)

    // def toMatriDD (colPos: Seq [Int], colPosV: Int, kind: MatrixKind = DENSE): (MatriD, VectorD)
    case class toMatriDD (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE)

    case class toMatriDI (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE)

    case class toMatriII (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE)

    // def toVectorD (colPos: Int)
    case class toVectorD (r: Relation, colPos: Int)
    case class toVectorI (r: Relation, colPos: Int)
    case class toVectorS (r: Relation, colPos: Int)
    case class toVectorD2 (r: Relation, colName: String)
    case class toVectorI2 (r: Relation, colName: String)
    case class toVectorS2 (r: Relation, colName: String)
    case class toRleVectorD (r: Relation, colPos: Int)
    case class toRleVectorI (r: Relation, colPos: Int)
    case class toRleVectorS (r: Relation, colPos: Int)
    case class toRleVectorD2 (r: Relation, colName: String)
    case class toRleVectorI2 (r: Relation, colName: String)
    case class toRleVectorS2 (r: Relation, colName: String)

}