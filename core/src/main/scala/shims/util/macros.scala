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

@macrocompat.bundle
class CaptureMacros(val c: whitebox.Context) extends OpenImplicitMacros {
  import c.universe._

  val Synthetic = weakTypeOf[shims.conversions.Synthetic]

  def materializeCapture[A: WeakTypeTag]: Tree = {
    val A = /*openImplicitTpeParam.getOrElse(*/weakTypeOf[A]/*)*/

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

    q"""_root_.shims.util.Capture[$A, $A]($tree0)"""
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
