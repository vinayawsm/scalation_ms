package scalation

import scalation.linalgebra.{MatriD, VectoD}

package object analytics
{

    // class ExpSmoothing (y_ : VectoD, ll: Int = 1, multiplicative : Boolean = false, validateSteps : Int = 1)
    // method - "Customized" or "Optimized"
    case class expSmoothing (method: String, t: VectoD, x: VectoD, l: Int = 1, m: Boolean = false, validateSteps: Int = 1, steps: Int = 1)

    // class ARIMA (t: VectoD, y: VectoD, d: Int = 0)
    // method - "AR", "MA", "ARMA"
    case class arima (method: String, t: VectoD, y: VectoD, d: Int = 0, p: Int = 1, q: Int = 1,
                      transBack: Boolean = true, steps: Int = 1)

    case class sarima (method: String, t: VectoD, y: VectoD, d: Int = 0, dd: Int = 0, period: Int = 1,
                       xxreg: MatriD = null, p: Int = 1, q: Int = 1 ,steps: Int = 1, xxreg_f : MatriD = null)
}