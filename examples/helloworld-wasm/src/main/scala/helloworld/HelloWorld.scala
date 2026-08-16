/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

object HelloWorld {
  def main(args: Array[String]): Unit = {
    /* System.out / println are unavailable on MinimalWasm (no WasmSystem).
     * Provide output via host imports or a library if needed.
     */
    val _ = "Hello world!"
  }
}
