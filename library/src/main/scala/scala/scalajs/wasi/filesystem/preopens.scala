package scala.scalajs.wasi.filesystem

package object preopens {

  // Type definitions
  type Descriptor = scala.scalajs.wasi.filesystem.types.Descriptor

  // Functions
  /** Return the set of preopened directories, and their path. */
  @scala.scalajs.wit.annotation.WitImport("wasi:filesystem/preopens@0.2.0", "get-directories")
  def getDirectories(): Array[scala.scalajs.wit.Tuple2[Descriptor, String]] =
    scala.scalajs.wit.native

}
