package scalation.preprocessing

import akka.actor.Actor
import scalation.columnar_db._

/**
  * Created by vinay on 11/8/18.
  */
class PreProcessingMaster extends Actor {

    def PPHandler(): Receive = {
        case "abc" => println("abc")
        case project (r, cNames) =>
            sender() ! r.project (cNames: _*)

        case mapToInt (v) =>
            sender ! mapToInt(v)

        case replaceMissingValues (r, mCol, mVal, fVal, frac) =>
            MissingValues.replaceMissingValues (r, mCol, mVal, fVal, frac)
            sender ! r

        case replaceMissingStrings (r, mCol, mVal, fVal, frac) =>
            MissingValues.replaceMissingStrings (r, mCol, mVal, fVal, frac)
            sender ! r
    }

    override def receive: Receive = PPHandler ()
}
