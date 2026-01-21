package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.exports.component.testing.Tests
import componentmodel.exports.component.testing.tests._

import scala.scalajs.WitUtils.toEither

import java.util.Optional

@WitImplementation
object TestsImpl extends Tests {

  override def roundtripPoint(a: Point): Point = a

  override def roundtripBasics1(a: wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double,
          Char]): wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char] = a

  override def roundtripListU16(a: Array[UShort]): Array[UShort] = a

  override def roundtripListPoint(a: Array[Point]): Array[Point] = a

  override def roundtripListVariant(a: Array[C1]): Array[C1] = a

  override def roundtripString(a: String): String = a

  override def roundtripC1(a: C1): C1 = a

  override def roundtripZ1(a: Z1): Z1 = a

  override def testC1(a: C1): Unit = {}

  override def roundtripEnum(a: E1): E1 = a

  override def roundtripTuple(a: wit.Tuple2[C1, Z1]): wit.Tuple2[C1, Z1] = a

  override def roundtripOption(a: Optional[String]): Optional[String] = a

  override def roundtripDoubleOption(a: Optional[Optional[String]]): Optional[Optional[String]] = a

  override def roundtripResult(a: wit.Result[Unit, Unit]): wit.Result[Unit, Unit] = a

  override def roundtripStringError(a: wit.Result[Float, String]): wit.Result[Float, String] = a

  override def roundtripEnumError(a: wit.Result[C1, E1]): wit.Result[C1, E1] = a

  override def roundtripF1(a: F1): F1 = a

  override def roundtripF2(a: F2): F2 = a

  override def roundtripF3(a: F3): F3 = a

  override def roundtripFlags(a: wit.Tuple2[F1, F1]): wit.Tuple2[F1, F1] = a

  override def roundtripTuple2(a: wit.Tuple2[Int, String]): wit.Tuple2[Int, String] = a

  override def roundtripTuple3(
      a: wit.Tuple3[Int, String, Boolean]): wit.Tuple3[Int, String, Boolean] = a

}
