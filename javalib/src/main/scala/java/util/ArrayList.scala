/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package java.util

import java.lang.Cloneable
import java.lang.Utils._
import java.util.ScalaOps._

import scala.scalajs._
import scala.scalajs.LinkingInfo.targetPureWasm

// _size keeps the track of the effective size of the underlying Array for Wasm.
// In JavaScript, we can use innerJS.length to obtain it's size.
class ArrayList[E] private (innerInit: AnyRef, private var _size: Int)
    extends AbstractList[E] with RandomAccess with Cloneable with Serializable {
  self =>

  private val innerJS =
    if (targetPureWasm) null
    else innerInit.asInstanceOf[js.Array[E]]

  private var innerWasm =
    if (!targetPureWasm) null
    else innerInit.asInstanceOf[Array[Any]]

  def this(initialCapacity: Int) = {
    this(
      if (targetPureWasm)
        new Array[Any](Math.max(initialCapacity, 0))
      else new js.Array[E],
      0
    )
    if (initialCapacity < 0)
      throw new IllegalArgumentException
  }

  def this() =
    this(
      if (targetPureWasm) new Array[Any](16)
      else new js.Array[E],
      0
    )

  def this(c: Collection[_ <: E]) = {
    this(
      if (targetPureWasm) new Array[Any](c.size())
      else new js.Array[E],
      0
    )
    addAll(c)
  }

  def trimToSize(): Unit = {
    // We ignore this as js.Array doesn't support explicit pre-allocation
  }

  def ensureCapacity(minCapacity: Int): Unit = {
    // We ignore this as js.Array doesn't support explicit pre-allocation
  }

  def size(): Int =
    if (targetPureWasm) _size
    else innerJS.length

  override def clone(): AnyRef =
    if (targetPureWasm) {
      new ArrayList(innerWasm.clone(), size())
    } else {
      new ArrayList(innerJS.jsSlice(0), 0)
    }

  def get(index: Int): E = {
    checkIndexInBounds(index)
    if (targetPureWasm) {
      innerWasm(index).asInstanceOf[E]
    } else {
      innerJS(index)
    }
  }

  override def set(index: Int, element: E): E = {
    val e = get(index)
    if (targetPureWasm) {
      innerWasm(index) = element
    } else {
      innerJS(index) = element
    }
    e
  }

  override def add(e: E): Boolean = {
    if (targetPureWasm) {
      add(_size, e)
    } else {
      innerJS.push(e)
    }
    true
  }

  override def add(index: Int, element: E): Unit = {
    checkIndexOnBounds(index)
    if (targetPureWasm) {
      if (size() >= innerWasm.length) expand()
      System.arraycopy(innerWasm, index, innerWasm, index + 1, size() - index)
      innerWasm(index) = element
      _size += 1
    } else {
      innerJS.splice(index, 0, element)
    }
  }

  override def remove(index: Int): E = {
    checkIndexInBounds(index)
    if (targetPureWasm) {
      val removed = innerWasm(index).asInstanceOf[E]
      System.arraycopy(innerWasm, index + 1, innerWasm, index, size() - index - 1)
      innerWasm(size - 1) = null
      _size -= 1
      removed
    } else {
      arrayRemoveAndGet(innerJS, index)
    }
  }

  override def clear(): Unit =
    if (targetPureWasm) {
      Arrays.fill(innerWasm.asInstanceOf[Array[AnyRef]], null)
      _size = 0
    } else {
      innerJS.length = 0
    }

  override def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    c match {
      case other: ArrayList[_] =>
        if (targetPureWasm) {
          checkIndexOnBounds(index)
          if (size() + other.size() >= innerWasm.length) expand(size() + other.size())
          System.arraycopy(innerWasm, index, innerWasm, index + other.size(), size() - index)
          System.arraycopy(other.innerWasm, 0, innerWasm, index, other.size())
          _size += c.size()
        } else {
          innerJS.splice(index, 0, other.innerJS.toSeq: _*)
        }
        other.size() > 0
      case _ => super.addAll(index, c)
    }
  }

  override protected def removeRange(fromIndex: Int, toIndex: Int): Unit =
    if (targetPureWasm) {
      if (fromIndex != toIndex) {
        if (fromIndex < 0 || fromIndex >= size() ||
            toIndex > size() || toIndex < fromIndex) {
          throw new IndexOutOfBoundsException()
        }
        System.arraycopy(innerWasm, toIndex, innerWasm, fromIndex, size() - toIndex)
        _size -= (toIndex - fromIndex)
      }
    } else {
      innerJS.splice(fromIndex, toIndex - fromIndex)
    }

  // Wasm only
  private def expand(): Unit = {
    expand(Math.max(innerWasm.length * 2, 1))
  }

  // Wasm only
  private def expand(newCapacity: Int): Unit = {
    innerWasm = Arrays.copyOf(innerWasm.asInstanceOf[Array[AnyRef]], newCapacity).asInstanceOf[Array[Any]]
  }
}
