/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import js.annotation._
import scala.scalajs.runtime.linkingInfo

object HelloWorld {
  def main(args: Array[String]): Unit = {
    import js.DynamicImplicits.truthValue

    val name = "a".getClass().getName()
    val isPrimitive = 1.getClass().isPrimitive()
    val isArray = Array(1, 2).getClass().isArray()
    val isInterface = 1.getClass().isPrimitive()
    val isInstance = "a".getClass().isInstance(1)
    val isAssignableFrom = classOf[CharSequence].isAssignableFrom(classOf[String])
    println(name)
    println(isPrimitive)
    println(isArray)
    println(isInterface)
    println(isInstance)
    println(isAssignableFrom)

    // if (linkingInfo.targetIsPureWasm) {
    //   println("WASI!")
    // } else {
    //   println("Wasm!")
    // }

    // if (js.typeOf(js.Dynamic.global.document) != "undefined" &&
    //     js.Dynamic.global.document &&
    //     js.Dynamic.global.document.getElementById("playground")) {
    //   sayHelloFromDOM()
    //   sayHelloFromTypedDOM()
    //   sayHelloFromJQuery()
    //   sayHelloFromTypedJQuery()
    // } else {
    //   println("Hello world!")
    // }
  }

  def sayHelloFromDOM(): Unit = {
    val document = js.Dynamic.global.document
    val playground = document.getElementById("playground")

    val newP = document.createElement("p")
    newP.innerHTML = "Hello world! <i>-- DOM</i>"
    playground.appendChild(newP)
  }

  def sayHelloFromTypedDOM(): Unit = {
    val document = window.document
    val playground = document.getElementById("playground")

    val newP = document.createElement("p")
    newP.innerHTML = "Hello world! <i>-- typed DOM</i>"
    playground.appendChild(newP)
  }

  def sayHelloFromJQuery(): Unit = {
    // val $ is fine too, but not very recommended in Scala code
    val jQuery = js.Dynamic.global.jQuery
    val newP = jQuery("<p>").html("Hello world! <i>-- jQuery</i>")
    newP.appendTo(jQuery("#playground"))
  }

  def sayHelloFromTypedJQuery(): Unit = {
    val jQuery = helloworld.JQuery
    val newP = jQuery("<p>").html("Hello world! <i>-- typed jQuery</i>")
    newP.appendTo(jQuery("#playground"))
  }
}

@js.native
@JSGlobalScope
object window extends js.Object {
  val document: DOMDocument = js.native

  def alert(msg: String): Unit = js.native
}

@js.native
trait DOMDocument extends js.Object {
  def getElementById(id: String): DOMElement = js.native
  def createElement(tag: String): DOMElement = js.native
}

@js.native
trait DOMElement extends js.Object {
  var innerHTML: String = js.native

  def appendChild(child: DOMElement): Unit = js.native
}

@js.native
@JSGlobal("jQuery")
object JQuery extends js.Object {
  def apply(selector: String): JQuery = js.native
}

@js.native
trait JQuery extends js.Object {
  def text(value: String): JQuery = js.native
  def text(): String = js.native

  def html(value: String): JQuery = js.native
  def html(): String = js.native

  def appendTo(parent: JQuery): JQuery = js.native
}
