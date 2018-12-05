package scalation.preprocessing

import akka.actor.Actor

import scalation.columnar_db._
import scalation.linalgebra.Converter

/**
  * Created by vinay on 11/8/18.
  */
class PreProcessingMaster extends Actor
{

    def PPHandler(): Receive =
    {
        case project (r, cNames, rName) =>
            sender() ! r.project (cNames: _*)

        case mapToInt (v, vName) =>
            sender ! Converter.mapToInt (v)

        case replaceMissingValues (r, mCol, mVal, fVal, frac, rName) =>
            MissingValues.replaceMissingValues (r, mCol, mVal, fVal, frac)
            sender ! r

        case replaceMissingStrings (r, mCol, mVal, fVal, frac, rName) =>
            MissingValues.replaceMissingStrings (r, mCol, mVal, fVal, frac)
            sender ! r

        case rmOutliers (method, c, args, vName) =>
            method match {
                case "DistanceOutlier"  => DistanceOutlier.rmOutliers (c, args: _*)
                case "QuantileOutlier"  => QuantileOutlier.rmOutliers (c, args: _*)
                case "QuartileXOutlier" => QuartileXOutlier.rmOutliers (c, args: _*)
            }
            sender ! c

        case impute (method, c, args, vName) =>
            val x = method match {
                case "Interpolate"         => Interpolate.impute (c, args: _*)
                case "ImputeMean"          => ImputeMean.impute (c, args: _*)
                case "ImputeNormal"        => ImputeNormal.impute (c, args: _*)
                case "ImputeMovingAverage" => ImputeMovingAverage.impute (c, args: _*)
            }
            sender() ! x

        // to matrix
        case toMatriD (r, colPos, kind, mName)  => sender() ! r.toMatriD (colPos, kind)
        case toMatriI (r, colPos, kind, mName)  => sender() ! r.toMatriI (colPos, kind)
        case toMatriI2 (r, colPos, kind, mName) => sender() ! r.toMatriI2 (colPos, kind)

        // to matrix + vector
        case toMatriDD (r, colPos, colPosV, kind, mName, vName) => sender() ! r.toMatriDD (colPos, colPosV, kind)
        case toMatriDI (r, colPos, colPosV, kind, mName, vName) => sender() ! r.toMatriDI (colPos, colPosV, kind)
        case toMatriII (r, colPos, colPosV, kind, mName, vName) => sender() ! r.toMatriII (colPos, colPosV, kind)

        // to vector
        case toVectorD (r, colPos, vName)      => sender() ! r.toVectorD (colPos)
        case toVectorI (r, colPos, vName)      => sender() ! r.toVectorI (colPos)
        case toVectorS (r, colPos, vName)      => sender() ! r.toVectorS (colPos)
        case toVectorD2 (r, colName, vName)    => sender() ! r.toVectorD (colName)
        case toVectorI2 (r, colName, vName)    => sender() ! r.toVectorI (colName)
        case toVectorS2 (r, colName, vName)    => sender() ! r.toVectorS (colName)
        case toRleVectorD (r, colPos, vName)   => sender() ! r.toRleVectorD (colPos)
        case toRleVectorI (r, colPos, vName)   => sender() ! r.toRleVectorI (colPos)
        case toRleVectorS (r, colPos, vName)   => sender() ! r.toRleVectorS (colPos)
        case toRleVectorD2 (r, colName, vName) => sender() ! r.toRleVectorD (colName)
        case toRleVectorI2 (r, colName, vName) => sender() ! r.toRleVectorI (colName)
        case toRleVectorS2 (r, colName, vName) => sender() ! r.toRleVectorS (colName)
    }

    override def receive: Receive = PPHandler ()
}
