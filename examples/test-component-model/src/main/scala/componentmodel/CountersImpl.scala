package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

/** The exported resource `component:testing/counters.counter`. */
@WitResourceExport(WitScope.unversioned("component", "testing", "counters"), "counter")
final class ExportedCounter(var value: Int)

object CountersImpl {
  @WitExport(WitScope.unversioned("component", "testing", "counters"), "[constructor]counter")
  def newCounter(@WitName("init") init: Int): ExportedCounter =
    new ExportedCounter(init)

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "[method]counter.get")
  def get(@WitName("self") self: wit.Borrow[ExportedCounter]): Int =
    self.value

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "[method]counter.add")
  def add(@WitName("self") self: wit.Borrow[ExportedCounter], @WitName("n") n: Int): Unit =
    self.value += n

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "[static]counter.merge")
  def merge(@WitName("a") a: wit.Borrow[ExportedCounter],
      @WitName("b") b: ExportedCounter): ExportedCounter =
    new ExportedCounter(a.value + b.value)

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "make-counter")
  def makeCounter(@WitName("init") init: Int): ExportedCounter =
    new ExportedCounter(init)

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "value-of")
  def valueOf(@WitName("c") c: wit.Borrow[ExportedCounter]): Int =
    c.value

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "consume")
  def consume(@WitName("c") c: ExportedCounter): Int =
    c.value

  @WitExport(WitScope.unversioned("component", "testing", "counters"), "maybe-counter")
  def maybeCounter(@WitName("init") init: wit.Option[Int]): wit.Option[ExportedCounter] =
    init.map(v => new ExportedCounter(v))
}
