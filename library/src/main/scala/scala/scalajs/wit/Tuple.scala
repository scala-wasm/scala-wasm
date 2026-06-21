/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalajs.wit

import scala.language.implicitConversions

final class Tuple1[+T1](val _1: T1)

object Tuple1 {
  def apply[T1](_1: T1): Tuple1[T1] = new Tuple1(_1)

  @inline implicit def fromScalaTuple1[T1](t: Tuple1[T1]): Tuple1[T1] =
    apply(t._1)

  @inline implicit def toScalaTuple1[T1](t: Tuple1[T1]): Tuple1[T1] =
    Tuple1(t._1)
}

final class Tuple2[+T1, +T2](val _1: T1, val _2: T2)

object Tuple2 {
  def apply[T1, T2](_1: T1, _2: T2): Tuple2[T1, T2] = new Tuple2(_1, _2)

  @inline implicit def fromScalaTuple2[T1, T2](t: (T1, T2)): Tuple2[T1, T2] =
    apply(t._1, t._2)

  @inline implicit def toScalaTuple2[T1, T2](t: Tuple2[T1, T2]): (T1, T2) =
    (t._1, t._2)
}

final class Tuple3[+T1, +T2, +T3](val _1: T1, val _2: T2, val _3: T3)

object Tuple3 {
  def apply[T1, T2, T3](_1: T1, _2: T2, _3: T3): Tuple3[T1, T2, T3] = new Tuple3(_1, _2, _3)

  @inline implicit def fromScalaTuple3[T1, T2, T3](t: (T1, T2, T3)): Tuple3[T1, T2, T3] =
    apply(t._1, t._2, t._3)

  @inline implicit def toScalaTuple3[T1, T2, T3](t: Tuple3[T1, T2, T3]): (T1, T2, T3) =
    (t._1, t._2, t._3)
}

final class Tuple4[+T1, +T2, +T3, +T4](val _1: T1, val _2: T2, val _3: T3, val _4: T4)

object Tuple4 {
  def apply[T1, T2, T3, T4](_1: T1, _2: T2, _3: T3, _4: T4): Tuple4[T1, T2, T3, T4] =
    new Tuple4(_1, _2, _3, _4)

  @inline implicit def fromScalaTuple4[T1, T2, T3, T4](
      t: (T1, T2, T3, T4)): Tuple4[T1, T2, T3, T4] = {
    apply(t._1, t._2, t._3, t._4)
  }

  @inline implicit def toScalaTuple4[T1, T2, T3, T4](t: Tuple4[T1, T2, T3, T4]): (T1, T2, T3, T4) =
    (t._1, t._2, t._3, t._4)
}

final class Tuple5[+T1, +T2, +T3, +T4, +T5](val _1: T1, val _2: T2, val _3: T3, val _4: T4,
    val _5: T5)

object Tuple5 {
  def apply[T1, T2, T3, T4, T5](_1: T1, _2: T2, _3: T3, _4: T4, _5: T5): Tuple5[T1, T2, T3, T4,
      T5] = {
    new Tuple5(_1, _2, _3, _4, _5)
  }

  @inline implicit def fromScalaTuple5[T1, T2, T3, T4, T5](
      t: (T1, T2, T3, T4, T5)): Tuple5[T1, T2, T3, T4, T5] = {
    apply(t._1, t._2, t._3, t._4, t._5)
  }

  @inline implicit def toScalaTuple5[T1, T2, T3, T4, T5](t: Tuple5[T1, T2, T3, T4, T5]): (T1, T2, T3,
      T4, T5) = {
    (t._1, t._2, t._3, t._4, t._5)
  }
}

final class Tuple6[+T1, +T2, +T3, +T4, +T5, +T6](val _1: T1, val _2: T2, val _3: T3, val _4: T4,
    val _5: T5, val _6: T6)

object Tuple6 {
  def apply[T1, T2, T3, T4, T5, T6](_1: T1, _2: T2, _3: T3, _4: T4, _5: T5, _6: T6): Tuple6[T1, T2,
      T3, T4, T5, T6] = {
    new Tuple6(_1, _2, _3, _4, _5, _6)
  }

  @inline implicit def fromScalaTuple6[T1, T2, T3, T4, T5, T6](
      t: (T1, T2, T3, T4, T5, T6)): Tuple6[T1, T2, T3, T4, T5, T6] = {
    apply(t._1, t._2, t._3, t._4, t._5, t._6)
  }

  @inline implicit def toScalaTuple6[T1, T2, T3, T4, T5, T6](t: Tuple6[T1, T2, T3, T4, T5, T6]): (T1,
      T2, T3, T4, T5, T6) = {
    (t._1, t._2, t._3, t._4, t._5, t._6)
  }
}

final class Tuple7[+T1, +T2, +T3, +T4, +T5, +T6, +T7](
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7
)

object Tuple7 {
  def apply[T1, T2, T3, T4, T5, T6, T7](
      _1: T1,
      _2: T2,
      _3: T3,
      _4: T4,
      _5: T5,
      _6: T6,
      _7: T7
  ): Tuple7[T1, T2, T3, T4, T5, T6, T7] = {
    new Tuple7(_1, _2, _3, _4, _5, _6, _7)
  }

  @inline implicit def fromScalaTuple7[T1, T2, T3, T4, T5, T6, T7](t: (T1, T2, T3, T4, T5, T6,
          T7)): Tuple7[T1, T2, T3, T4, T5, T6, T7] = {
    apply(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }

  @inline implicit def toScalaTuple7[T1, T2, T3, T4, T5, T6, T7](
      t: Tuple7[T1, T2, T3, T4, T5, T6, T7]): (T1, T2, T3, T4, T5, T6, T7) = {
    (t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }
}

final class Tuple8[+T1, +T2, +T3, +T4, +T5, +T6, +T7, +T8](
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7,
    val _8: T8
)

object Tuple8 {
  def apply[T1, T2, T3, T4, T5, T6, T7, T8](
      _1: T1,
      _2: T2,
      _3: T3,
      _4: T4,
      _5: T5,
      _6: T6,
      _7: T7,
      _8: T8
  ): Tuple8[T1, T2, T3, T4, T5, T6, T7, T8] = {
    new Tuple8(_1, _2, _3, _4, _5, _6, _7, _8)
  }

  @inline implicit def fromScalaTuple8[T1, T2, T3, T4, T5, T6, T7, T8](t: (T1, T2, T3, T4, T5, T6,
          T7, T8)): Tuple8[T1, T2, T3, T4, T5, T6, T7, T8] = {
    apply(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  }

  @inline implicit def toScalaTuple8[T1, T2, T3, T4, T5, T6, T7, T8](
      t: Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]): (T1, T2, T3, T4, T5, T6, T7, T8) = {
    (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  }
}

final class Tuple9[+T1, +T2, +T3, +T4, +T5, +T6, +T7, +T8, +T9](
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7,
    val _8: T8,
    val _9: T9
)

object Tuple9 {
  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](
      _1: T1,
      _2: T2,
      _3: T3,
      _4: T4,
      _5: T5,
      _6: T6,
      _7: T7,
      _8: T8,
      _9: T9
  ): Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9] = {
    new Tuple9(_1, _2, _3, _4, _5, _6, _7, _8, _9)
  }

  @inline implicit def fromScalaTuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](t: (T1, T2, T3, T4, T5,
          T6, T7, T8, T9)): Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9] = {
    apply(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)
  }

  @inline implicit def toScalaTuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](
      t: Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): (T1, T2, T3, T4, T5, T6, T7, T8, T9) = {
    (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)
  }
}

final class Tuple10[+T1, +T2, +T3, +T4, +T5, +T6, +T7, +T8, +T9, +T10](
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7,
    val _8: T8,
    val _9: T9,
    val _10: T10
)

object Tuple10 {
  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
      _1: T1,
      _2: T2,
      _3: T3,
      _4: T4,
      _5: T5,
      _6: T6,
      _7: T7,
      _8: T8,
      _9: T9,
      _10: T10
  ): Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] = {
    new Tuple10(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10)
  }

  @inline implicit def fromScalaTuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](t: (T1, T2, T3, T4,
          T5, T6, T7, T8, T9, T10)): Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] = {
    apply(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10)
  }

  @inline implicit def toScalaTuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
      t: Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]): (T1, T2, T3, T4, T5, T6, T7, T8, T9,
      T10) = {
    (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10)
  }
}
