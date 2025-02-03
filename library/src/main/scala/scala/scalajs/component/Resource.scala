package scala.scalajs.component

trait Resource {
  def close(): Unit = native
}