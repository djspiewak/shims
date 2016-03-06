package shims.util

import language.experimental.macros
import reflect.macros.whitebox

trait =/=[A, B]

object =/= {

  implicit def materialize[A, B]: A =/= B = macro NegMacros.materialize[A, B]
}

@macrocompat.bundle
class NegMacros(val c: whitebox.Context) {
  import c.universe._

  def materialize[A, B](implicit A: WeakTypeTag[A], B: WeakTypeTag[B]): Tree = {
    if (/*!A.tpe.typeSymbol.isAbstract &&*/ A.tpe =:= B.tpe)
      c.abort(c.enclosingPosition, s"type ${A.tpe} is equal to type ${B.tpe}")
    else
      q"""new =/=[$A, $B] {}"""
  }
}