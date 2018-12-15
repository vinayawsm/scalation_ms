package scalation.analytics

import akka.actor.{Actor, ActorSystem, Props}

import scalation.analytics.classifier._
import scalation.analytics.forecaster._
import scalation.analytics._
import scalation.linalgebra._

class AnalyticsMaster extends Actor
{
    val actorSystem = ActorSystem ("AnalyticsMasterTest")
    val an = actorSystem.actorOf(Props[AnalyticsWorker], "root")

    var ci: Map[String, ClassifierInt] = Map[String, ClassifierInt]()

    def analyticsHandler(): Receive =
    {
        // ClassifierInt
        case ClassifierInt (model, rName) =>
            an ! ClassifierIntIn (model, rName)

        case ClassifierInt_classify (name, z, xx, rName) =>
            an ! ClassifierIntIn_classify (name, z, xx, rName)

        case ClassifierInt_test (name, itest, xx, yy, rName) =>
            an ! ClassifierIntIn_test (name, itest, xx, yy, rName)

        case ClassifierInt_crossValidate (name, nx, show) =>
            an ! ClassifierIntIn_crossValidate (name, nx, show)

        case ClassifierInt_crossValidateRand (name, nx, show) =>
            an ! ClassifierIntIn_crossValidateRand (name, nx, show)

        case ClassifierInt_featureSelection (name, tol) =>
            an ! ClassifierIntIn_featureSelection (name, tol)

        case ClassifierInt_calcCorrelation (name, rName) =>
            an ! ClassifierIntIn_calcCorrelation (name, rName)

        case ClassifierInt_calcCorrelation2 (name, zrg, xrg, rName) =>
            an ! ClassifierIntIn_calcCorrelation2 (name, zrg, xrg, rName)

        // ClassifierReal
        case ClassifierReal (model, rName) =>
            an ! ClassifierRealIn (model, rName)

        case ClassifierReal_classify (name, z, xx, rName) =>
            an ! ClassifierRealIn_classify (name, z, xx, rName)

        case ClassifierReal_test (name, itest, xx, yy, rName) =>
            an ! ClassifierRealIn_test (name, itest, xx, yy, rName)

        case ClassifierReal_crossValidate (name, nx, show) =>
            an ! ClassifierRealIn_crossValidate (name, nx, show)

        case ClassifierReal_crossValidateRand (name, nx, show) =>
            an ! ClassifierRealIn_crossValidateRand (name, nx, show)

        case ClassifierReal_featureSelection (name, tol) =>
            an ! ClassifierRealIn_featureSelection (name, tol)

        case ClassifierReal_calcCorrelation (name, rName) =>
            an ! ClassifierRealIn_calcCorrelation (name, rName)

        case ClassifierReal_calcCorrelation2 (name, zrg, xrg, rName) =>
            an ! ClassifierRealIn_calcCorrelation2 (name, zrg, xrg, rName)

        // PredictorVec
        case PredictorVec_m (model, rName) =>
            an ! PredictorVecIn_m (model, rName)

        case PredictorVec_train (name, yy, rName) =>
            an ! PredictorVecIn_train (name, yy, rName)

        case PredictorVec_predict (name, d, v, rName) =>
            an ! PredictorVecIn_predict (name, d, v, rName)

        case PredictorVec_crossValidate (name, algor, k, rando, rName) =>
            an ! PredictorVecIn_crossValidate (name, algor, k, rando, rName)

        // PredictorMat
        case PredictorMat_m (model, rName) =>
            an ! PredictorMatIn_m (model, rName)

        case PredictorMat_train (name, yy, rName) =>
            an ! PredictorMatIn_train (name, yy, rName)

        case PredictorMat_predict (name, v, z, rName) =>
            an ! PredictorMatIn_predict (name, v, z, rName)

        case PredictorMat_crossValidate (name, algor, k, rando, rName) =>
            an ! PredictorMatIn_crossValidate (name, algor, k, rando, rName)

    }

    override def receive: Receive = analyticsHandler()
}

// runMain scalation.analytics.AnalyticsMasterTest1
object AnalyticsMasterTest1 extends App {


    val actorSystem = ActorSystem ("AnalyticsMasterTest")
    val analytics = actorSystem.actorOf(Props[AnalyticsMaster], "root")

    val xy = new MatrixI ((10, 4), 1, 0, 1, 1,                 // data matrix
        1, 0, 1, 0,
        1, 0, 1, 1,
        0, 0, 1, 0,
        0, 0, 0, 1,
        0, 1, 0, 0,
        0, 1, 0, 1,
        0, 1, 1, 0,
        1, 1, 0, 0,
        1, 0, 0, 1)

    val fn = Array ("Color", "Type", "Origin")                 // feature/variable names
    val k  = 2                                                 // number of classes
    val cn = Array ("No", "Yes")                               // class names
    val vc = null.asInstanceOf [Array [Int]]                   // use default value count
    val me = 1E-3f                                             // me-estimates
    val th = 0.0                                               // threshold

    println ("---------------------------------------------------------------")
    println ("D A T A   M A T R I X")
    println ("xy = " + xy)

    analytics ! ClassifierInt (BayesClassifier(xy,fn,k,cn,vc,me), "nb")



    Thread.sleep(10000)
    actorSystem.terminate()
}