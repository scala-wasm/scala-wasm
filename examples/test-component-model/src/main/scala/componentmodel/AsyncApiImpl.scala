package componentmodel

import scala.scalajs.wit.annotation._

object AsyncApiImpl {

  @WitAsync
  @WitExport(WitScope.unversioned("component", "testing", "async-api"), "add-one")
  def addOne(@WitName("a") a: Int): Int = a + 1

}
