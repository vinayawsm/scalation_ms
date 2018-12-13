package scalation

import scalation.linalgebra._

package object analytics
{

    type Strings = Array [String]

    // ClassifierInt
    case class ClassifierInt (model: analytics.classifier.ClassifierInt, name: String)
    case class ClassifierIntIn (model: analytics.classifier.ClassifierInt, name: String)

    case class ClassifierInt_classify (name: String, z: VectoD = null, xx: MatriI = null, rName: String)
    case class ClassifierInt_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriI = null, yy: VectoI = null, rName: String)
    case class ClassifierInt_featureSelection (name: String, tol: Double = 0.01)
    case class ClassifierInt_calcCorrelation (name: String, rName: String)
    case class ClassifierInt_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)
    case class ClassifierIntIn_classify (name: String, z: VectoD = null, xx: MatriI = null, rName: String)
    case class ClassifierIntIn_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriI = null, yy: VectoI = null, rName: String)
    case class ClassifierIntIn_featureSelection (name: String, tol: Double)
    case class ClassifierIntIn_calcCorrelation (name: String, rName: String)
    case class ClassifierIntIn_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)


    // ClassifierReal
    case class ClassifierReal (model: analytics.classifier.ClassifierReal, name: String)
    case class ClassifierRealIn (model: analytics.classifier.ClassifierReal, name: String)

    case class ClassifierReal_classify (name: String, z: VectoD = null, xx: MatriD = null, rName: String)
    case class ClassifierReal_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriD = null, yy: VectoI = null, rName: String)
    case class ClassifierReal_featureSelection (name: String, tol: Double)
    case class ClassifierReal_calcCorrelation (name: String, rName: String)
    case class ClassifierReal_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)
    case class ClassifierRealIn_classify (name: String, z: VectoD = null, xx: MatriD = null, rName: String)
    case class ClassifierRealIn_test (name: String, itest: IndexedSeq [Int] = null, xx: MatriD = null, yy: VectoI = null, rName: String)
    case class ClassifierRealIn_featureSelection (name: String, tol: Double)
    case class ClassifierRealIn_calcCorrelation (name: String, rName: String)
    case class ClassifierRealIn_calcCorrelation2 (name: String, zrg: Range, xrg: Range, rName: String)

    // PredictorVec
    case class PredictorVec_m (model: analytics.PredictorVec, name: String)
    case class PredictorVecIn_m (model: analytics.PredictorVec, name: String)

    case class PredictorVec_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorVec_predict (name: String, d: Double = -1.0, v: VectoD = null, rName: String)
    case class PredictorVec_crossValidate (name: String, algor: (VectoD, VectoD, Int) => PredictorVec, k: Int = 10,
                                           rando: Boolean = true, rName: String)
    case class PredictorVecIn_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorVecIn_predict (name: String, d: Double = -1.0, v: VectoD = null, rName: String)
    case class PredictorVecIn_crossValidate (name: String, algor: (VectoD, VectoD, Int) => PredictorVec, k: Int = 10,
                                           rando: Boolean = true, rName: String)

    // PredictorMat
    case class PredictorMat_m (model: analytics.PredictorMat, name: String)
    case class PredictorMatIn_m (model: analytics.PredictorMat, name: String)

    case class PredictorMat_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorMat_predict (name: String, v: VectoD = null, z: MatriD = null, rName: String)
    case class PredictorMat_crossValidate (name: String, algor: (MatriD, VectoD) => PredictorMat, k: Int = 10,
                                           rando: Boolean = true, rName: String)
    case class PredictorMatIn_train (name: String, yy: VectoD = null, rName: String)
    case class PredictorMatIn_predict (name: String, v: VectoD = null, z: MatriD = null, rName: String)
    case class PredictorMatIn_crossValidate (name: String, algor: (MatriD, VectoD) => PredictorMat, k: Int = 10,
                                             rando: Boolean = true, rName: String)


}