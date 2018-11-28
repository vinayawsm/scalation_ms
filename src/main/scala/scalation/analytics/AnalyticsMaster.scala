package scalation.analytics

import akka.actor.Actor

import scalation.analytics.forecaster.ExpSmoothing
import scalation.plot.Plot

/**
  * Created by vinay on 11/28/18.
  */
class AnalyticsMaster extends Actor {

    def analyticsHandler(): Receive = {
        case expSmoothing (method, t, x, l, m, validateSteps) =>
            val ts = new ExpSmoothing(x)
            method match {
                case "Customized" =>
                    ts.smooth ()
                    ts.eval ()
                    val s = ts.fittedValues ()
                    new Plot (t, x, s, "Customized: Plot of x, new_x vs. t")
                case "Optimized" =>
                    ts.train ()
                    ts.eval()
                    val s = ts.fittedValues ()
                    new Plot (t, x, s, "Optimized: Plot of x, new_x vs. t")
            }
    }

    override def receive: Receive = analyticsHandler()
}
