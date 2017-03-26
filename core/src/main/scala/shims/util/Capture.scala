package shims.util

import scala.annotation.implicitNotFound

@implicitNotFound("unable to find an implicit value of type $A")
private[shims] final case class Capture[A, T](value: A) extends AnyVal

private[shims] object Capture {
  implicit def materialize[A <: AnyRef](implicit A: A): Capture[A, A.type] = Capture(A)
}
