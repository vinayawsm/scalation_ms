package scalation.preprocessing

import akka.actor.Actor

/**
  * Created by vinay on 11/8/18.
  */
class PreProcessingMaster extends Actor {

    def PPHandler(): Receive = {
        case "abc" => println("abc")
        case project (r, cNames) =>
            sender() ! r.project (cNames: _*)


    }

    override def receive: Receive = PPHandler ()
}
