package wasmsystemtest.wasi.cli

import scala.scalajs.wit
import scala.scalajs.wit.annotation.{WitImport, WitScope}
import scala.scalajs.wit.native

package object environment {

  @WitImport(WitScope("wasi", "cli", "environment", "0.2.0"), "get-environment")
  def getEnvironment(): Array[wit.Tuple2[String, String]] = native

  @WitImport(WitScope("wasi", "cli", "environment", "0.2.0"), "initial-cwd")
  def initialCwd(): wit.Option[String] = native

}
