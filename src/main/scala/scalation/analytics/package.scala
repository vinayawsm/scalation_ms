package scalation

import scalation.linalgebra._

package object analytics
{

    type Strings = Array [String]

    // abstract class ClassifierInt (x: MatriI, y: VectoI, protected var fn: Strings = null,
    //                              k: Int, protected var cn: Strings = null)
    // method: which method to use
    //          (BayesClassifier, DecisionTreeID3, NullModel)
    // sbc: sub-bayes classifier method
    //          {Naive Bayes, 1-BAN, TAN Bayes, 2-BAN-OS}
    // modelName: name with which this model should be saved as
    case class ClassifierInt (method: String, x: MatriI = null, y: VectoI = null, fn: Strings = null, k: Int, cn: Strings = null,
                              vc: Array[Int] = null, me: Float = 0, th: Double = 0.0, sbc: String = "Naive Bayes", modelName: String)


    case class ClassifierReal (method: String, x: MatriD = null, xv: VectorD = null, y: VectoI = null, fn: Strings = null,
                               k: Int = 2, cn: Strings = null, isConst: Array [Boolean], vc: Array[Int] = null, td: Int = 0,
                               nF: Int = 1, bR: Double = 1.0, fS: Int = 1, s: Int = 223, modelName: String)


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