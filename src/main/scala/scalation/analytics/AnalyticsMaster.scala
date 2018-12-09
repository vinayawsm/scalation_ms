package scalation.analytics

import akka.actor.{Actor, ActorSystem, Props}

import scalation.analytics.classifier._
import scalation.analytics.forecaster._
import scalation.linalgebra.{MatrixD, VectorD}
import scalation.plot.Plot
import scalation.random.Random

class AnalyticsMaster extends Actor
{

    var ci: Map[String, ClassifierInt] = Map[String, ClassifierInt]()

    def analyticsHandler(): Receive =
    {
        // classifiers
        case ClassifierInt (method, x, y, fn, k, cn, vc, me, th, sbc, modelName) =>
            method match {
                case "BayesClassifier" =>
                    val bc = sbc match {
                        case "Naive Bayes"      =>  BayesClassifier (x, fn, k, cn, vc, me)
                        case "1-BAN"            =>  BayesClassifier (x, fn, k, cn, vc, me, th)
                        case "TAN Bayes"        =>  BayesClassifier (x, fn, k, cn, me, vc)
                        case "2-BAN-OS"         =>  BayesClassifier (x, fn, k, cn, vc, th, me)
                    }
                    // save bc to map. What should the map type be? Map[String, ? ]
                    // ? = classifierInt doesn't look working
                    // ci += (modelName -> bc.asInstanceOf[ClassifierInt])
                    // ci(modelName).featureSelection()

                case "DecisionTreeID3" =>
                    val tree = DecisionTreeID3.test (x, fn, k, cn, vc)

                case "NullModel" =>
                    val nm = new classifier.NullModel (y, k, cn)
            }

        case ClassifierReal (method, x, xv, y, fn, k, cn, isConst, vc, td, nf, br, fs, s, modelName) =>
            method match {
                case "NaiveBayesR" =>
                    val nbr = new NaiveBayesR (x, y, fn, k, cn)
                case "LogisticRegression" =>
                    val lrg = new LogisticRegression (x, y, fn, cn)
                case "LDA" =>
                    val lda = new LDA (x.asInstanceOf [MatrixD], y, fn, k, cn)
                case "KNN_Classifier" =>
                    val knn = new KNN_Classifier (x, y, fn, k, cn)
                case "DecisionTreeC45" =>
                    val tree = classifier.DecisionTreeC45.test (x, fn, isConst, k, cn, vc, td)
                case "RandomForest" =>
                    val rF = new RandomForest (x.asInstanceOf [MatrixD], y, nf, br, fs, k, s, fn, cn)
                case "SimpleLDA" =>
                    val slda = new SimpleLDA (xv, y, fn, k, cn)
                case "SimpleLogisticRegression" =>
                    val slr = new SimpleLogisticRegression (x, y, fn, cn)
                case "SupportVectorMachine" =>
                    val svm = new SupportVectorMachine (x.asInstanceOf [MatrixD], y, fn, cn)
            }

        /*

        case expSmoothing (method, t, x, l, m, validateSteps, steps) =>
            val ts = new ExpSmoothing(x, l, m, validateSteps)
            method match {
                case "Customized" =>
                    ts.smooth ()
                    ts.eval ()
                    val s = ts.fittedValues ()
                    println (s"fit = ${ts.fit}")
                    println (s"predict (s) = ${ts.forecast (steps)}")
                    new Plot (t, x, s, "Customized: Plot of x, new_x vs. t")
                case "Optimized" =>
                    ts.train ()
                    ts.eval()
                    val s = ts.fittedValues ()
                    println (s"fit = ${ts.fit}")
                    println (s"predict (s2) = ${ts.forecast (steps)}")
                    new Plot (t, x, s, "Optimized: Plot of x, new_x vs. t")
            }

        case arima (method, t, y, d, p, q, transBack, steps) =>
            val ts = new ARIMA (t, y, d)
            method match {
                case "AR" =>
                    val φ_a = ts.est_ar (p)
                    println (s"φ_a = $φ_a")
                    new Plot (t, y, ts.predict_ar (transBack) + ts.mu, s"Plot of y, ar($p) vs. t")
                    val ar_f = ts.forecast_ar (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using AR($p) model = $ar_f")
                case "MA" =>
                    val θ_a = ts.est_ma (q)
                    println (s"θ_a = $θ_a")
                    new Plot (t, y, ts.predict_ma (transBack) + ts.mu, s"Plot of y, ma($q) vs. t")
                    val ma_f = ts.forecast_ma (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using MA($q) model = $ma_f")
                case "ARMA" =>
                    val (φ, θ) = ts.est_arma (p, q)
                    println (s"φ = $φ, θ = $θ")
                    new Plot (t, y, ts.predict_arma (transBack) + ts.mu, s"Plot of y, arma($p, $q) vs. t")
                    val arma_f = ts.forecast_arma (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using ARMA($p, $q) model = $arma_f")
            }

        case sarima (method, t, y, d, dd, period, xxreg, p, q ,steps, xxreg_f) =>
            val ts = new SARIMA (y, d, dd, period, xxreg)
            method match {
                case "AR" =>
                    ts.setPQ (p)
                    ts.train ()
                    new Plot (t, y, ts.fittedValues (), s"Plot of y, ar($p) vs. t", true)
                    val ar_f = ts.forecast (steps, xxreg_f)
                    println (s"$steps-step ahead forecasts using AR($p) model = $ar_f")
                case "MA" =>
                    ts.setPQ (0, q)
                    ts.train ()
                    new Plot (t, y, ts.fittedValues (), s"Plot of y, ma($q) vs. t", true)
                    val ma_f = ts.forecast (steps, xxreg_f)
                    println (s"$steps-step ahead forecasts using MA($q) model = $ma_f")
                case "ARMA" =>
                    ts.setPQ (p, q)
                    ts.train ()
                    new Plot (t, y, ts.fittedValues (), s"Plot of y, arma($p, $q) vs. t", true)
                    val arma_f = ts.forecast (steps, xxreg_f)
                    println (s"$steps-step ahead forecasts using ARMA($p, $q) model = $arma_f")
            }
*/

    }

    override def receive: Receive = analyticsHandler()
}

// runMain scalation.analytics.AnalyticsMasterTest
object AnalyticsMasterTest extends App {
    val actorSystem = ActorSystem ("AnalyticsMasterTest")
    val actor = actorSystem.actorOf(Props[AnalyticsMaster], "root")

    val n = 100
    val r = Random ()
    val t = VectorD.range (0, n)
    val y = VectorD (for (i <- 0 until n) yield t(i) + 10.0 * r.gen)
    actor ! expSmoothing ("Optimized", t, y)

    actor ! arima ("AR", t, y)

    Thread.sleep(10000)
    actorSystem.terminate()
}