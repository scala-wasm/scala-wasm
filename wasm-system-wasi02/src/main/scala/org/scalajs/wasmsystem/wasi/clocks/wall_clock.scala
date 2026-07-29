package org.scalajs.wasmsystem.wasi.clocks

import scala.scalajs.wit.annotation.{WitImport, WitRecord}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UInt, ULong}

package object wall_clock {

  /** A time and date in seconds plus nanoseconds. */
  @WitRecord
  final class Datetime(val seconds: ULong, val nanoseconds: UInt) {
    override def equals(other: Any): Boolean = other match {
      case that: Datetime => this.seconds == that.seconds && this.nanoseconds == that.nanoseconds
      case _              => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + seconds.hashCode()
      result = 31 * result + nanoseconds.hashCode()
      result
    }

    override def toString(): String = "Datetime(" + seconds + ", " + nanoseconds + ")"
  }

  object Datetime {
    def apply(seconds: ULong, nanoseconds: UInt): Datetime = new Datetime(seconds, nanoseconds)
  }

  @WitImport("wasi:clocks/wall-clock@0.2.0", "now")
  def now(): Datetime = native

}
