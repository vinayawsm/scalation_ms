package scalation.preprocessing

import akka.actor.Actor

import scalation.columnar_db._

/**
  * Created by vinay on 11/8/18.
  */
class PreProcessingMaster extends Actor
{

    def PPHandler(): Receive =
    {
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

        case rmOutliers (method, c, args) =>
            method match {
                case "DistanceOutlier"  => DistanceOutlier.rmOutliers (c, args: _*)
                case "QuantileOutlier"  => QuantileOutlier.rmOutliers (c, args: _*)
                case "QuartileXOutlier" => QuartileXOutlier.rmOutliers (c, args: _*)
            }
            sender ! c

        case impute (method, c, args) =>
            val x = method match {
                case "Interpolate"         => Interpolate.impute (c, args: _*)
                case "ImputeMean"          => ImputeMean.impute (c, args: _*)
                case "ImputeNormal"        => ImputeNormal.impute (c, args: _*)
                case "ImputeMovingAverage" => ImputeMovingAverage.impute (c, args: _*)
            }
            sender() ! x

        // to matrix
        case toMatriD (r, colPos, kind)  => sender() ! r.toMatriD (colPos, kind)
        case toMatriI (r, colPos, kind)  => sender() ! r.toMatriI (colPos, kind)
        case toMatriI2 (r, colPos, kind) => sender() ! r.toMatriI2 (colPos, kind)

        // to matrix + vector
        case toMatriDD (r, colPos, colPosV, kind) => sender() ! r.toMatriDD (colPos, colPosV, kind)
        case toMatriDI (r, colPos, colPosV, kind) => sender() ! r.toMatriDI (colPos, colPosV, kind)
        case toMatriII (r, colPos, colPosV, kind) => sender() ! r.toMatriII (colPos, colPosV, kind)

        // to vector
        case toVectorD (r, colPos)      => sender() ! r.toVectorD (colPos)
        case toVectorI (r, colPos)      => sender() ! r.toVectorI (colPos)
        case toVectorS (r, colPos)      => sender() ! r.toVectorS (colPos)
        case toVectorD2 (r, colName)    => sender() ! r.toVectorD (colName)
        case toVectorI2 (r, colName)    => sender() ! r.toVectorI (colName)
        case toVectorS2 (r, colName)    => sender() ! r.toVectorS (colName)
        case toRleVectorD (r, colPos)   => sender() ! r.toRleVectorD (colPos)
        case toRleVectorI (r, colPos)   => sender() ! r.toRleVectorI (colPos)
        case toRleVectorS (r, colPos)   => sender() ! r.toRleVectorS (colPos)
        case toRleVectorD2 (r, colName) => sender() ! r.toRleVectorD (colName)
        case toRleVectorI2 (r, colName) => sender() ! r.toRleVectorI (colName)
        case toRleVectorS2 (r, colName) => sender() ! r.toRleVectorS (colName)
    }

    override def receive: Receive = PPHandler ()
}
