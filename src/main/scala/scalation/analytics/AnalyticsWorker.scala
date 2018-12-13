package scalation.analytics

import akka.actor.Actor

import scalation.analytics
import scalation.linalgebra.{MatriD, VectoD, VectoI}
import scalation.stat.Statistic

/**
  * Created by vinay on 12/10/18.
  */
class AnalyticsWorker extends Actor {

    var doubleMap : Map [String, Double] = Map [String, Double]()
    var matriDMap : Map [String, MatriD] = Map [String, MatriD]()
    var vectoDMap : Map [String, VectoD] = Map [String, VectoD]()
    var vectoIMap : Map [String, VectoI] = Map [String, VectoI]()

    var ci : Map[String, analytics.classifier.ClassifierInt] = Map[String, analytics.classifier.ClassifierInt]()
    var ciClassify: Map [String, (Int, String, Double)] = Map [String, (Int, String, Double)]()

    var cr : Map[String, analytics.classifier.ClassifierReal] = Map[String, analytics.classifier.ClassifierReal]()
    var crClassify: Map [String, (Int, String, Double)] = Map [String, (Int, String, Double)]()

    var pv : Map [String, analytics.PredictorVec] = Map [String, PredictorVec]()
    var pvRegression: Map [String, analytics.Regression] = Map [String, analytics.Regression]()
    var pvArStMap : Map [String, Array[Statistic]] = Map [String, Array[Statistic]]()

    var pr : Map [String, analytics.PredictorMat] = Map [String, PredictorMat]()
    var prArStMap : Map [String, Array[Statistic]] = Map [String, Array[Statistic]]()


    def analyticsWorkerHandler(): Receive =
    {
        // ClassifierInt
        case ClassifierIntIn (model, rName) =>
            ci += (rName -> model)

        case ClassifierInt_classify (name, z, xx, rName) =>
            if (z == null) vectoIMap += (rName -> ci(name).classify (xx))
            else ciClassify += (rName -> ci(name).classify (z))

        case ClassifierIntIn_test (name, itest, xx, yy, rName) =>
            if (itest == null) doubleMap += (rName -> ci(name).test (xx, yy))
            else doubleMap += (rName -> ci(name).test (itest))

        case ClassifierIntIn_featureSelection (name, tol) =>
            ci(name).featureSelection (tol)

        case ClassifierIntIn_calcCorrelation (name, rName) =>
            matriDMap += (rName -> ci(name).calcCorrelation)

        case ClassifierIntIn_calcCorrelation2 (name, zrg, xrg, rName) =>
            matriDMap += (rName -> ci(name).calcCorrelation2(zrg, xrg))


        // ClassifierReal
        case ClassifierRealIn (model, rName) =>
            cr += (rName -> model)

        case ClassifierReal_classify (name, z, xx, rName) =>
            if (z == null) vectoIMap += (rName -> cr(name).classify (xx))
            else crClassify += (rName -> cr(name).classify (z))

        case ClassifierRealIn_test (name, itest, xx, yy, rName) =>
            if (itest == null) doubleMap += (rName -> cr(name).test (xx, yy))
            else doubleMap += (rName -> cr(name).test (itest))

        case ClassifierRealIn_featureSelection (name, tol) =>
            cr(name).featureSelection (tol)

        case ClassifierRealIn_calcCorrelation (name, rName) =>
            matriDMap += (rName -> cr(name).calcCorrelation)

        case ClassifierRealIn_calcCorrelation2 (name, zrg, xrg, rName) =>
            matriDMap += (rName -> cr(name).calcCorrelation2(zrg, xrg))


        // PredictorVec
        case PredictorVecIn_m (model, rName) =>
            pv += (rName -> model)

        case PredictorVecIn_train (name, yy, rName) =>
            pvRegression += (rName -> pv(name).train(yy))

        case PredictorVecIn_predict (name, d, v, rName) =>
            if (v == null) doubleMap += (rName -> pv(name).predict(d))
            else doubleMap += (rName -> pv(name).predict(v))

        case PredictorVecIn_crossValidate (name, algor, k, rando, rName) =>
            pvArStMap += (rName -> pv(name).crossValidate(algor, k, rando))


        // PredictorMat
        case PredictorMatIn_m (model, rName) =>
            pr += (rName -> model)

        case PredictorMatIn_train (name, yy, rName) =>
            pr += (rName -> pr(name).train(yy))

        case PredictorMatIn_predict (name, v, z, rName) =>
            if (z == null) doubleMap += (rName -> pr(name).predict(v))
            else vectoDMap += (rName -> pr(name).predict(z))

        case PredictorMatIn_crossValidate (name, algor, k, rando, rName) =>
            prArStMap += (rName -> pr(name).crossValidate(algor, k, rando))

    }

    override def receive: Receive = analyticsWorkerHandler ()
}
