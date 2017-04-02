package shims.util

import scala.annotation.implicitNotFound
import scala.util.{Either, Left, Right}

@implicitNotFound("unable to find an implicit value of type ${A}")
final case class Capture[A, T](value: A) extends AnyVal

object Capture {
  implicit def materialize[A <: AnyRef](implicit A: A): Capture[A, A.type] = Capture(A)
}

@implicitNotFound("unable to find an implicit value of type ${A} or ${B}")
final case class EitherCapture[A, B, T](value: Either[A, B]) extends AnyVal

trait EitherLowPriorityImplicits {

  implicit def materializeLeft[A <: AnyRef, B](implicit A: A): EitherCapture[A, B, A.type] =
    EitherCapture(Left(A))
}

object EitherCapture extends EitherLowPriorityImplicits {

  implicit def materializeRight[A, B <: AnyRef](implicit B: B): EitherCapture[A, B, B.type] =
    EitherCapture(Right(B))
}

// this is defined for all A
final case class OptionCapture[A, T](value: Option[A]) extends AnyVal

trait OptionLowPriorityImplicits {

  implicit def materializeNone[A]: OptionCapture[A, Any] =
    OptionCapture(None)
}

object OptionCapture extends OptionLowPriorityImplicits {

  implicit def materializeSome[A <: AnyRef](implicit A: A): OptionCapture[A, A.type] =
    OptionCapture(Some(A))
}
