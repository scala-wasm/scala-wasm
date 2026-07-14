package org.scalajs.linker.interface

/** A directory containing WIT files and the world to embed.
 *
 *  The linker embeds each input as a `component-type*` custom section.
 *
 *  If the WIT files contain only one world, `world` may be omitted. Linking
 *  fails if they contain multiple worlds and `world` is omitted.
 *
 *  @param directory directory containing WIT files
 *  @param world world to embed
 */
final class WasmComponentWitInput private (
    val directory: String,
    val world: Option[String]
) {
  override def equals(that: Any): Boolean = that match {
    case that: WasmComponentWitInput =>
      this.directory == that.directory &&
      this.world == that.world
    case _ =>
      false
  }

  override def hashCode(): Int =
    31 * directory.## + world.##

  override def toString(): String =
    s"WasmComponentWitInput($directory, $world)"
}

object WasmComponentWitInput {
  def apply(directory: String, world: Option[String] = None): WasmComponentWitInput =
    new WasmComponentWitInput(directory, world)
}
