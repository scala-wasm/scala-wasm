package scala.scalajs.component.unsigned

final class UByte(
    private[scalajs] val underlyingValue: Byte
) extends java.io.Serializable
    with Comparable[UByte] {

    @inline final def toByte: Byte = underlyingValue
    @inline final def toShort: Short = toInt.toShort
    @inline final def toChar: Char = toInt.toChar
    @inline final def toInt: Int = underlyingValue & 0xff
    @inline final def toLong: Long = toInt.toLong
    @inline final def toFloat: Float = toInt.toFloat
    @inline final def toDouble: Double = toInt.toDouble

    override def compareTo(o: UByte): Int =
      (underlyingValue & 0xff) - (o.underlyingValue & 0xff)
}