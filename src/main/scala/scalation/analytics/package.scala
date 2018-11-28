package scalation

import scalation.linalgebra.VectoD

package object analytics {

    // class ExpSmoothing (y_ : VectoD, ll: Int = 1, multiplicative : Boolean = false, validateSteps : Int = 1)
    // method - "Customized" or "Optimized"
    case class expSmoothing (method: String, t: VectoD, x: VectoD, l: Int = 1, m: Boolean = false, validateSteps: Int = 1, steps: Int = 1)

    // class ARIMA (t: VectoD, y: VectoD, d: Int = 0)
    // if this doesn't work out, may be we should break into serperae classes for AR, MA, ARMA
    // method - "AR", "MA", "ARMA"
    case class arima (method: String, t: VectoD, y: VectoD, d: Int = 0, p: Int = 1, q: Int = 1, steps: Int = 1)
}