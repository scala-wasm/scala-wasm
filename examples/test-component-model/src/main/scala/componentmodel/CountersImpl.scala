package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

/** The exported resource `component:testing/counters.counter`. */
@WitResourceExport(WitScope.unversioned("component", "testing", "counters"), "counter")
final class ExportedCounter(private var value: Int) {
  @WitResourceMethod("get")
  def get(): Int = value

  @WitResourceMethod("add")
  def add(@WitName("n") n: Int): Unit =
    value += n
}

object ExportedCounter {
  @WitResourceConstructor
  def apply(@WitName("init") init: Int): ExportedCounter =
    new ExportedCounter(init)

  @WitResourceStaticMethod("merge")
  def merge(@WitName("a") a: wit.Borrow[ExportedCounter],
      @WitName("b") b: ExportedCounter): ExportedCounter = {
    new ExportedCounter(a.get() + b.get())
  }
}

object CountersImpl {
  @WitExport(WitScope.unversioned("component", "testing", "counters"), "make-counter")
  def makeCounter(@WitName("init") init: Int): ExportedCounter =
    new ExportedCounter(init)

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "value-of")
  def valueOf(@WitName("c") c: wit.Borrow[ExportedCounter]): Int =
    c.get()

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "consume")
  def consume(@WitName("c") c: ExportedCounter): Int =
    c.get()

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "maybe-counter")
  def maybeCounter(@WitName("init") init: wit.Option[Int]): wit.Option[ExportedCounter] =
    init.map(v => new ExportedCounter(v))

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "sum")
  def sum(@WitName("cs") cs: Array[ExportedCounter]): Int =
    cs.map(_.get()).sum
}
