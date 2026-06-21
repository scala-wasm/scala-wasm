/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

object HelloWorld {
  def main(args: Array[String]): Unit = {
    val start = System.currentTimeMillis()
    println(s"Hello world!")
    val end = System.currentTimeMillis()
    println(s"println took ${end - start} millis")
  }
}
