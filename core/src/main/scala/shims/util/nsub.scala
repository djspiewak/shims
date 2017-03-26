package shims.util

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@implicitNotFound("unable to prove that $A is NOT a subtype of $B (likely because it actually is)")
sealed trait </<[A, B]

object </< {
  def unsafeLie[A, B] = new </<[A, B] {}

  implicit def materialize[A, B]: A </< B = macro NegMacros.materialize[A, B]
}

@macrocompat.bundle
class NegMacros(val c: whitebox.Context) {
  import c.universe._

  def materialize[A, B](implicit A: WeakTypeTag[A], B: WeakTypeTag[B]): Tree = {
    if (/*!A.tpe.typeSymbol.isAbstract &&*/ A.tpe <:< B.tpe)
      c.abort(c.enclosingPosition, s"${A.tpe} is a subtype of ${B.tpe}")
    else
      q"""_root_.shims.util.</<.unsafeLie[$A, $B]"""
  }
}
