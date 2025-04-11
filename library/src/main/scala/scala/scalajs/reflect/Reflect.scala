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

package scala.scalajs.reflect

import scala.collection.mutable

import scala.scalajs.js

final class LoadableModuleClass private[reflect] (
    val runtimeClass: Class[_],
    loadModuleFun: () => Any
) {
  /** Loads the module instance and returns it. */
  def loadModule(): Any = loadModuleFun()
}

final class InstantiatableClass private[reflect] (
    val runtimeClass: Class[_],
    val declaredConstructors: List[InvokableConstructor]
) {
  /** Instantiates a new instance of this class using the zero-argument
   *  constructor.
   *
   *  @throws java.lang.InstantiationException (caused by a
   *    `NoSuchMethodException`)
   *    If this class does not have a public zero-argument constructor.
   */
  def newInstance(): Any = {
    getConstructor().fold[Any] {
      throw new InstantiationException(runtimeClass.getName).initCause(
          new NoSuchMethodException(runtimeClass.getName + ".<init>()"))
    } { ctor =>
      ctor.newInstance()
    }
  }

  /** Looks up a public constructor identified by the types of its formal
   *  parameters.
   *
   *  If no such public constructor exists, returns `None`.
   */
  def getConstructor(parameterTypes: Class[_]*): Option[InvokableConstructor] =
    declaredConstructors.find(_.parameterTypes.sameElements(parameterTypes))
}

final class InvokableConstructor private[reflect]  (
    val parameterTypes: List[Class[_]],
    newInstanceFun: Array[Any] => Any
) {
  def newInstance(args: Any*): Any = {
    /* Check the number of actual arguments. We let the casts and unbox
     * operations inside `newInstanceFun` take care of the rest.
     */
    require(args.size == parameterTypes.size)
    newInstanceFun(args.toArray)
  }
}

object Reflect {
  private val loadableModuleClasses =
    mutable.HashMap.empty[String, LoadableModuleClass]

  private val instantiatableClasses =
    mutable.HashMap.empty[String, InstantiatableClass]

  @deprecated("used only by deprecated code", since = "1.19.0")
  @js.native
  private trait JSFunctionVarArgs extends js.Function {
    def apply(args: Any*): Any
  }

  // `protected[reflect]` makes these methods public in the IR

  @deprecated("use registerLoadableModuleClass2 instead", since = "1.19.0")
  protected[reflect] def registerLoadableModuleClass[T](
      fqcn: String, runtimeClass: Class[T],
      loadModuleFun: js.Function0[T]): Unit = {
    registerLoadableModuleClass2(fqcn, runtimeClass, loadModuleFun)
  }

  @deprecated("use registerInstantiatableClass2 instead", since = "1.19.0")
  protected[reflect] def registerInstantiatableClass[T](
      fqcn: String, runtimeClass: Class[T],
      constructors: js.Array[js.Tuple2[js.Array[Class[_]], JSFunctionVarArgs]]): Unit = {

    registerInstantiatableClass2(fqcn, runtimeClass, constructors.map { c =>
      (c._1.toArray, (args: Array[Any]) => c._2(args: _*))
    }.toArray)
  }

  protected[reflect] def registerLoadableModuleClass2[T](
      fqcn: String, runtimeClass: Class[T],
      loadModuleFun: () => T): Unit = {
    loadableModuleClasses(fqcn) =
      new LoadableModuleClass(runtimeClass, loadModuleFun)
  }

  protected[reflect] def registerInstantiatableClass2[T](
      fqcn: String, runtimeClass: Class[T],
      constructors: Array[(Array[Class[_]], Array[Any] => Any)]): Unit = {
    val invokableConstructors = constructors.map { c =>
      new InvokableConstructor(c._1.toList, c._2)
    }
    instantiatableClasses(fqcn) =
      new InstantiatableClass(runtimeClass, invokableConstructors.toList)
  }

  /** Reflectively looks up a loadable module class.
   *
   *  A module class is the technical term referring to the class of a Scala
   *  `object`. The object or one of its super types (classes or traits) must
   *  be annotated with
   *  [[scala.scalajs.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the object must be "static", i.e., declared at the top-level of
   *  a package or inside a static object.
   *
   *  If the module class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was not static, this method
   *  returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the module class, including its trailing `$`
   */
  def lookupLoadableModuleClass(fqcn: String): Option[LoadableModuleClass] =
    loadableModuleClasses.get(fqcn)

  /** Reflectively looks up an instantiable class.
   *
   *  The class or one of its super types (classes or traits) must be annotated
   *  with
   *  [[scala.scalajs.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the class must not be abstract, nor be a local class (i.e., a
   *  class defined inside a `def`). Inner classes (defined inside another
   *  class) are supported.
   *
   *  If the class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was abstract or local, this
   *  method returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   */
  def lookupInstantiatableClass(fqcn: String): Option[InstantiatableClass] =
    instantiatableClasses.get(fqcn)
}
