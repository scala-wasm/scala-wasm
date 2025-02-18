package scala.scalajs.component.annotation

import scala.annotation.meta._

/**
  * Represents bitflags in the Component Model using a final case class
  * where all fields are of type `Boolean`.
  *
  * Example:
  *
  * {{{
  * @ComponentFlags
  * final case class F1(
  *   b0: Boolean, b1: Boolean, b2: Boolean, b3: Boolean,
  *   b4: Boolean, b5: Boolean, b6: Boolean, b7: Boolean
  * )
  * }}}
  *
  * Is there a better alternative bitflags representation in Scala?
  * Note that these interfaces must also be usable in `javalib`,
  * meaning Scala libs (e.g. Scala's `Enumeration`) cannot be used.
  *
  * A possible workaround is to define a type alias:
  *
  * {{{
  * type Flags = Int
  * }}}
  *
  * This allows function signatures to use `Flags`, which will be interpreted
  * as a flag in the Component Model. For example:
  *
  * {{{
  * def roundtripFlags(a: Flags): Flags = cm.native
  * }}}
  *
  * When an `Int` is passed as an argument, it will be serialized/deserialized
  * as a flag according to the Component Model's specifications.
  */
@field @getter @setter
class ComponentFlags extends scala.annotation.StaticAnnotation