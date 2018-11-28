package scalation.analytics

import akka.actor.{Actor, ActorSystem, Props}

import scalation.analytics.forecaster._
import scalation.linalgebra.VectorD
import scalation.plot.Plot
import scalation.random.Random

/**
  * Created by vinay on 11/28/18.
  */
class AnalyticsMaster extends Actor {

    def analyticsHandler(): Receive = {
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

        case arima (method, t, y, d, p, q, steps) =>
            val ts = new ARIMA (t, y, d)
            method match {
                case "AR" =>
                    val φ_a = ts.est_ar (p)
                    println (s"φ_a = $φ_a")
                    new Plot (t, y, ts.predict_ar () + ts.mu, s"Plot of y, ar($p) vs. t")
                    val ar_f = ts.forecast_ar (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using AR($p) model = $ar_f")
                case "MA" =>
                    val θ_a = ts.est_ma (q)
                    println (s"θ_a = $θ_a")
                    new Plot (t, y, ts.predict_ma () + ts.mu, s"Plot of y, ma($q) vs. t")
                    val ma_f = ts.forecast_ma (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using MA($q) model = $ma_f")
                case "ARMA" =>
                    val (φ, θ) = ts.est_arma (p, q)
                    println (s"φ = $φ, θ = $θ")
                    new Plot (t, y, ts.predict_arma () + ts.mu, s"Plot of y, arma($p, $q) vs. t")
                    val arma_f = ts.forecast_arma (steps) + ts.mu
                    println (s"$steps-step ahead forecasts using ARMA($p, $q) model = $arma_f")
            }
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
}