package shims

import scalaz.\/

trait AsScalaz[-I, +O] {
  def c2s(i: I): O
}

trait AsCats[-I, +O] {
  def s2c(i: I): O
}

trait EitherConversions {

  implicit def eitherAsScalaz[A, B] = new AsScalaz[Either[A, B], A \/ B] with AsCats[A \/ B, Either[A, B]] {
    def c2s(e: Either[A, B]) = \/.fromEither(e)
    def s2c(e: A \/ B) = e.fold(l => Left(l), r => Right(r))
  }
}
