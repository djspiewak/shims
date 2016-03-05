package shims.syntax

import scala.util.{Either, Left, Right}

object either {
  type \/[+A, +B] = Either[A, B]

  object \/- {
    def apply[A, B](b: B): A \/ B = Right(b)
    def unapply[A, B](either: Either[A, B]): Option[B] = either.right.toOption
  }

  object -\/ {
    def apply[A, B](a: A): A \/ B = Left(a)
    def unapply[A, B](either: Either[A, B]): Option[A] = either.left.toOption
  }

  implicit class EitherSyntax[A, B](val either: Either[A, B]) extends AnyVal {

    def flatMap[C](f: B => Either[A, C]): Either[A, C] = either.right flatMap f

    def flatten[C](implicit ev: B =:= Either[A, C]): Either[A, C] = flatMap(ev)

    def map[C](f: B => C): Either[A, C] = either.right map f
  }
}