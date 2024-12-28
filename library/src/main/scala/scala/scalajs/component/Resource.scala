package scala.scalajs.component

trait Resource extends java.io.Closeable {
  final override def close(): Unit = native
}