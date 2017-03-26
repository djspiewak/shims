package shims.util

import scala.annotation.implicitNotFound

@implicitNotFound("unable to prove that $A is NOT a subtype of $B (likely because it actually is)")
sealed trait </<[A, B]

private[util] trait NSubLowPriorityImplicits {
  implicit def universal[A, B]: A </< B = null
}

object </< extends NSubLowPriorityImplicits {
  implicit def lie1[A, B](implicit ev: A <:< B): A </< B = null
  implicit def lie2[A, B](implicit ev: A <:< B): A </< B = null
}
