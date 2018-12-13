package scalation

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scalation.analytics.{PredictorMat, PredictorVec}
import scalation.columnar_db.TableObj.Row
import scalation.linalgebra._
import scalation.columnar_db.{Imputation, ImputeMean, Relation, Table}
import scalation.linalgebra.MatrixKind.MatrixKind

package object master
{

    ///////////////////////////////////////////////////////////////// databases

    /** Boolean function that uses the value for the given column name (String)
      *  in the predicate (e.g., used by 'where' and 'filter')
      */
    type Predicate [T] = (String, T => Boolean)


    // persistence methods
    case class saveRelation (n: Seq[String])
    case class dropRelation (n: Seq[String])
    case class loadRelation (n: Seq[String])

    // Relation methods
    case class createFromCSV (fname: Seq[String], name: Seq[String], colname: Seq[String], key: Int, domain: String,
                              skip: Int = 0, eSep: String = ",")

    case class show (name: Seq [String], limit: Int = 5)

    // def select [T : ClassTag] (cName: String, p: T => Boolean): Relation
    case class select [T: ClassTag] (name: Seq[String], rName: Seq[String], p: Predicate[T])

    // def project (cName: String*): Relation
    case class project (name: Seq[String], rName: Seq[String], cNames: Seq[String])

    // def union (r2: Table): Relation
    case class union (name: Seq[String], rName: String)
    case class unionReply (r: Relation, rName: String, t: Int, uc: String)

    // def minus (r2: Table): Relation
    case class minus (name: Seq [String], name2: Seq[String], rName: Seq[String])

    // def product (r2: Table): Relation
    case class product (name: Seq [String], name2: Seq[String], rName: Seq[String])

    // def join (r2: Table): Table
    case class join (name: Seq [String], name2: Seq[String], rName: Seq[String])

    // def intersect (_r2: Table): Relation
    case class intersect (name: Seq [String], name2: Seq[String], rName: Seq[String])

    case class getRelation (name: Seq[String], rName: String)

    case class relReply (id: String, r: Relation, rName: String, t: Int = 1)

    case class msgReply (rName: String, uc: String, t: Int, method: String)

    case class delete (name: Seq [String])

    case object nameAll



    ///////////////////////////////////////////////////////////// preprocessing

    // def project (cName: String*): Relation
    case class pp_project (r: Relation, cNames: Seq[String], rName: String)

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

    // ClassifierInt
    case class ClassifierInt (model: analytics.classifier.ClassifierInt, name: String)

    case class ClassifierInt_classify (name: String, z: VectoD = null, xx: MatriI = null, rName: String)
    case class ClassifierInt_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriI = null, yy: VectoI = null, rName: String)
    case class ClassifierInt_featureSelection (name: String, tol: Double = 0.01)
    case class ClassifierInt_calcCorrelation (name: String, rName: String)
    case class ClassifierInt_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)

    // ClassifierReal
    case class ClassifierReal (model: analytics.classifier.ClassifierReal, name: String)

    case class ClassifierReal_classify (name: String, z: VectoD = null, xx: MatriD = null, rName: String)
    case class ClassifierReal_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriD = null, yy: VectoI = null, rName: String)
    case class ClassifierReal_featureSelection (name: String, tol: Double)
    case class ClassifierReal_calcCorrelation (name: String, rName: String)
    case class ClassifierReal_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)

    // PredictorVec
    case class PredictorVec_m (model: analytics.PredictorVec, name: String)

    case class PredictorVec_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorVec_predict (name: String, d: Double = -1.0, v: VectoD = null, rName: String)
    case class PredictorVec_crossValidate (name: String, algor: (VectoD, VectoD, Int) => PredictorVec, k: Int = 10,
                                           rando: Boolean = true, rName: String)

    // PredictorMat
    case class PredictorMat_m (model: analytics.PredictorMat, name: String)

    case class PredictorMat_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorMat_predict (name: String, v: VectoD = null, z: MatriD = null, rName: String)
    case class PredictorMat_crossValidate (name: String, algor: (MatriD, VectoD) => PredictorMat, k: Int = 10,
                                           rando: Boolean = true, rName: String)


    //////////////////////////////////////////////////////////// result methods

    case class relReply2 (id: String, r: Relation, rName: String)

    case class recvRelation (rName: String, r: Relation)

    case class recvMatrixD (mName: String, m: MatrixD)

    case class recvMatrixI (mName: String, m: MatrixI)

    case class recvMatrixS (mName: String, m: MatriS)

    case class recvVectoD (vName: String, v: VectoD)

    case class recvVectoI (vName: String, v: VectoI)

    case class recvVectoS (vName: String, v: VectoS)


    case class printPaths ()
}
