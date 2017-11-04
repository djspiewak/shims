/*
 * Copyright 2017 Daniel Spiewak
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

@macrocompat.bundle
class CaptureMacros(val c: whitebox.Context) extends OpenImplicitMacros {
  import c.universe._

  val Synthetic = weakTypeOf[shims.conversions.Synthetic]

  def materializeCapture[A: WeakTypeTag]: Tree = {
    val A = openImplicitTpeParam.getOrElse(weakTypeOf[A])

    q"""_root_.shims.util.Capture[$A](${reconstructImplicit(A)})"""
  }

  def materializeEitherCapture[A: WeakTypeTag, B: WeakTypeTag]: Tree = {
    val A = openImplicitTpeParam.getOrElse(weakTypeOf[A])
    val B = openImplicitTpeParam.getOrElse(weakTypeOf[B])

    val treeAM = try {
      Right(q"_root_.scala.util.Left(${reconstructImplicit(A)})")
    } catch {
      case e: Exception => Left(e)
    }

    val treeBM = try {
      Right(q"_root_.scala.util.Right(${reconstructImplicit(B)})")
    } catch {
      case e: Exception => Left(e)
    }

    val result = treeAM.right.map(Right(_)).right.getOrElse(treeBM).fold(
      throw _,    // it's so great how scalac uses exceptions...
      identity)

    q"""_root_.shims.util.EitherCapture[$A, $B]($result)"""
  }

  def materializeOptionCapture[A: WeakTypeTag]: Tree = {
    val A = openImplicitTpeParam.getOrElse(weakTypeOf[A])

    val result = try {
      q"_root_.scala.Some(${reconstructImplicit(A)})"
    } catch {
      case e: Exception => q"_root_.scala.None"
    }

    q"""_root_.shims.util.OptionCapture[$A]($result)"""
  }

  private def reconstructImplicit(A: Type): Tree = {
    // we catch the invalid diverging implicit expansion error and produce a correct one
    // this works around a bug in NSC; we can remove this on Dotty
    val tree0 = try {
      c.inferImplicitValue(A)
    } catch {
      case e: Exception => c.abort(c.enclosingPosition, s"Implicit $A not found")
    }

    if (tree0 == EmptyTree) {
      c.abort(c.enclosingPosition, s"Implicit $A not found")
    }

    if (tree0.tpe <:< Synthetic) {
      c.abort(c.enclosingPosition, s"Cannot capture subtype of of Synthetic")
    }

    tree0
  }
}

// copied from shapeless
@macrocompat.bundle
private[shims] trait OpenImplicitMacros {
  val c: whitebox.Context

  import c.universe._

  def openImplicitTpe: Option[Type] =
    c.openImplicits.headOption.map(_.pt)

  def openImplicitTpeParam: Option[Type] =
    openImplicitTpe.map {
      case TypeRef(_, _, List(tpe)) =>
        tpe.map(_.dealias)
      case other =>
        c.abort(c.enclosingPosition, s"Bad materialization: $other")
    }

  def secondOpenImplicitTpe: Option[Type] =
    c.openImplicits match {
      case (List(_, second, _ @ _*)) =>
        Some(second.pt)
      case _ => None
    }
}
