package scalation

import scalation.linalgebra.VectoD

package object analytics {

    // class ExpSmoothing (y_ : VectoD, ll: Int = 1, multiplicative : Boolean = false, validateSteps : Int = 1)
    // method - "Customized" or "Optimized"
    case class expSmoothing (method: String, t: VectoD, x: VectoD, l: Int = 1, m: Boolean = false, validateSteps: Int = 1)
}