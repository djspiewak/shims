package shims

trait FlatMap[F[_]] extends Functor[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}

object FlatMap {
  def apply[F[_]](implicit F: FlatMap[F]): FlatMap[F] = F
}