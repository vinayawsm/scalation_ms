package scalation

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scalation.linalgebra._
import scalation.columnar_db.{Imputation, ImputeMean, Relation, Table}
import scalation.linalgebra.MatrixKind.MatrixKind

package object master
{

    ///////////////////////////////////////////////////////////////// databases

    // persistence methods
    case class saveRelation (n: String)
    case class dropRelation (n: String)
    case class getRelation (n: String)
    case class getRelReply (n: String, r: Relation)



    ///////////////////////////////////////////////////////////// preprocessing

    // def project (cName: String*): Relation
    case class project (r: Relation, cNames: Seq[String], rName: String)

    // def mapToInt (s: VectoS): (VectoI, BiMap [StrNum, Int])
    case class mapToInt (v: VectoS, vName: String)

    // def replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    // return the table xy to sender after computation
    case class replaceMissingValues [T <: Any : ClassTag] (xy: Table, missingCol: String, missingVal: T,
                                                           funcVal: Imputation = ImputeMean, fraction: Double = 0.2, rName: String)

    // def replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
    //                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2)
    // return the table xy to sender after computation
    case class replaceMissingStrings (xy: Table, missingCol: String, missingStr: String = "?",
                                      funcVal: Imputation = ImputeMean, fraction: Double = 0.2, rName: String)

    // def rmOutliers (c: Vec, args: Double*)
    // method can be - 'DistanceOutlier', 'QuantileOutlier', 'QuartileXOutlier'
    case class rmOutliers (method: String, c: Vec, args: Seq[Double], vName: String)

    // def impute (c: Vec, args: Int*): Any
    // method can be - 'Interpolate', 'ImputeMean', 'ImputeNormal', 'ImputeMovingAverage'
    case class impute (method: String, c: Vec, args: Seq[Int], vName: String)

    // def toMatriD (colPos: Seq [Int], kind: MatrixKind = DENSE): MatriD
    case class toMatriD (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE, mName: String)

    case class toMatriI (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE, mName: String)

    case class toMatriI2 (r: Relation, colPos: Seq [Int], kind: MatrixKind = MatrixKind.DENSE, mName: String)

    // def toMatriDD (colPos: Seq [Int], colPosV: Int, kind: MatrixKind = DENSE): (MatriD, VectorD)
    case class toMatriDD (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE, mName: String, vName: String)

    case class toMatriDI (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE, mName: String, vName: String)

    case class toMatriII (r: Relation, colPos: Seq [Int], colPosV: Int, kind: MatrixKind = MatrixKind.DENSE, mName: String, vName: String)

    // def toVectorD (colPos: Int)
    case class toVectorD (r: Relation, colPos: Int, vName: String)
    case class toVectorI (r: Relation, colPos: Int, vName: String)
    case class toVectorS (r: Relation, colPos: Int, vName: String)
    case class toVectorD2 (r: Relation, colName: String, vName: String)
    case class toVectorI2 (r: Relation, colName: String, vName: String)
    case class toVectorS2 (r: Relation, colName: String, vName: String)
    case class toRleVectorD (r: Relation, colPos: Int, vName: String)
    case class toRleVectorI (r: Relation, colPos: Int, vName: String)
    case class toRleVectorS (r: Relation, colPos: Int, vName: String)
    case class toRleVectorD2 (r: Relation, colName: String, vName: String)
    case class toRleVectorI2 (r: Relation, colName: String, vName: String)
    case class toRleVectorS2 (r: Relation, colName: String, vName: String)



    ///////////////////////////////////////////////////////////////// analytics

    // class ExpSmoothing (y_ : VectoD, ll: Int = 1, multiplicative : Boolean = false, validateSteps : Int = 1)
    // method - "Customized" or "Optimized"
    case class expSmoothing (method: String, t: VectoD, x: VectoD, l: Int = 1, m: Boolean = false, validateSteps: Int = 1, steps: Int = 1)

    // class ARIMA (t: VectoD, y: VectoD, d: Int = 0)
    // method - "AR", "MA", "ARMA"
    case class arima (method: String, t: VectoD, y: VectoD, d: Int = 0, p: Int = 1, q: Int = 1,
                      transBack: Boolean = true, steps: Int = 1)

    case class sarima (method: String, t: VectoD, y: VectoD, d: Int = 0, dd: Int = 0, period: Int = 1,
                       xxreg: MatriD = null, p: Int = 1, q: Int = 1 ,steps: Int = 1, xxreg_f : MatriD = null)


    //////////////////////////////////////////////////////////// result methods

    case class relReply2 (id: String, r: Relation, rName: String)

    case class recvRelation (rName: String, r: Relation)

    case class recvMatrixD (mName: String, m: MatrixD)

    case class recvMatrixI (mName: String, m: MatrixI)

    case class recvMatrixS (mName: String, m: MatriS)

    case class recvVectoD (vName: String, v: VectoD)

    case class recvVectoI (vName: String, v: VectoI)

    case class recvVectoS (vName: String, v: VectoS)
}
