package scala.scalajs.wasi.cli

package object environment {

  // Functions
  /** Get the POSIX-style environment variables.
   *
   *  Each environment variable is provided as a pair of string variable names
   *  and string value.
   *
   *  Morally, these are a value import, but until value imports are available
   *  in the component model, this import function should return the same
   *  values each time it is called.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/environment@0.2.0", "get-environment")
  def getEnvironment(): Array[scala.scalajs.wit.Tuple2[String, String]] = scala.scalajs.wit.native

  /** Get the POSIX-style arguments to the program. */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/environment@0.2.0", "get-arguments")
  def getArguments(): Array[String] = scala.scalajs.wit.native

  /** Return a path that programs should use as their initial current working
   *  directory, interpreting `.` as shorthand for this.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/environment@0.2.0", "initial-cwd")
  def initialCwd(): java.util.Optional[String] = scala.scalajs.wit.native

}
