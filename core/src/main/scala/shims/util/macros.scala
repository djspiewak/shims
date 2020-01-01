/*
 * Copyright 2020 Daniel Spiewak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shims.util

import scala.reflect.macros.whitebox
import scala.util.{Left, Right}

class CaptureMacros(val c: whitebox.Context) extends OpenImplicitMacros {
  import c.universe._

  val Synthetic = weakTypeOf[shims.conversions.Synthetic]

  def materializeCapture[A: WeakTypeTag]: Tree = {
    val A = openImplicitTpeParam.getOrElse(weakTypeOf[A])
    q"""_root_.shims.util.Capture[$A](${reconstructImplicit(A)})"""
  }

  def materializeEitherCapture[A: WeakTypeTag, B: WeakTypeTag]: Tree = {
    val treeAM = try {
      val A = leftImplicitTpeParam.getOrElse(weakTypeOf[A])
      val B = weakTypeOf[B]
      Right(q"""_root_.shims.util.EitherCapture[$A, $B](_root_.scala.util.Left[$A, $B](${reconstructImplicit(A)}))""")
    } catch {
      // ok it's actually an Error, not an Exception 🤦‍♀️
      case t: Throwable => Left(t)
    }

    val treeBM = try {
      val A = weakTypeOf[A]
      val B = rightImplicitTpeParam.getOrElse(weakTypeOf[B])
      Right(q"""_root_.shims.util.EitherCapture[$A, $B](_root_.scala.util.Right[$A, $B](${reconstructImplicit(B)}))""")
    } catch {
      // ok it's actually an Error, not an Exception 🤦‍♀️
      case t: Throwable => Left(t)
    }

    treeAM.fold(_ => treeBM, Right(_)).fold(
      throw _,    // it's so great how scalac uses exceptions...
      identity)
  }

  def materializeOptionCapture[A: WeakTypeTag]: Tree = {
    try {
      val A = openImplicitTpeParam.getOrElse(weakTypeOf[A])
      q"""_root_.shims.util.OptionCapture[$A](_root_.scala.Some(${reconstructImplicit(A)}))"""
    } catch {
      // ok it's actually an Error, not an Exception 🤦‍♀️
      case t: Throwable =>
        q"""_root_.shims.util.OptionCapture[${weakTypeOf[A]}](_root_.scala.None)"""
    }
  }

  private def reconstructImplicit(A: Type): Tree = {
    // we catch the invalid diverging implicit expansion error and produce a correct one
    // this works around a bug in NSC; we can remove this on Dotty
    val tree0 = CaptureMacros.around(c)(A) {
      try {
        c.inferImplicitValue(A)
      } catch {
        case t: Throwable => c.abort(c.enclosingPosition, s"implicit $A not found")
      }
    }

    if (tree0 == EmptyTree) {
      c.abort(c.enclosingPosition, s"implicit $A not found")
    }

    if (tree0.tpe <:< Synthetic) {
      c.abort(c.enclosingPosition, s"cannot capture subtype of Synthetic")
    }

    tree0
  }
}

/*
 * Implicit macros which directly call into inferImplicitValue are
 * responsible for their own divergence checking (https://github.com/scala/bug/issues/10584#issuecomment-343712505).
 * Thus, we maintain a thread local as we move up and down the implicit
 * search tree.  Note that we explicitly do not carry this thread local
 * between sibling descents, so as to avoid unsoundness with scoping.
 * At any rate, this does a relatively basic <:< check to figure out whether
 * or not we're looking at the same type that we saw previously at a higher
 * level of the subtree.
 */
private[util] object CaptureMacros {
  val outstanding = new ThreadLocal[List[Any]] {
    override def initialValue = List()
  }

  def around[A](c: whitebox.Context)(tpe: c.universe.Type)(body: => A): A = {
    val vs = outstanding.get()

    // simple divergence check
    if (vs.exists(tpe <:< _.asInstanceOf[c.universe.Type])) {
      c.abort(c.enclosingPosition, s"implicit $tpe not found")
    } else {
      val vs2 = tpe :: vs

      outstanding.set(vs2)
      val back = body
      outstanding.set(vs)

      back
    }
  }
}

// copied from shapeless
private[shims] trait OpenImplicitMacros {
  val c: whitebox.Context

  import c.universe._

  def openImplicitTpe: Option[Type] =
    c.openImplicits.headOption.map(_.pt)

  def openImplicitTpeParam: Option[Type] =
    openImplicitTpe map {
      case TypeRef(_, _, List(tpe)) =>
        tpe.map(_.dealias)

      case other =>
        c.abort(c.enclosingPosition, s"bad materialization: $other")
    }

  def leftImplicitTpeParam: Option[Type] =
    openImplicitTpe map {
      case TypeRef(_, _, List(tpe, _)) =>
        tpe.map(_.dealias)

      case other =>
        c.abort(c.enclosingPosition, s"bad materialization (left): $other")
    }

  def rightImplicitTpeParam: Option[Type] =
    openImplicitTpe map {
      case TypeRef(_, _, List(_, tpe)) =>
        tpe.map(_.dealias)

      case other =>
        c.abort(c.enclosingPosition, s"bad materialization (right): $other")
    }
}
