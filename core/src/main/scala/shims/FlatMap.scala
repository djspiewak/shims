package shims

trait FlatMap[F[_]] extends Functor[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}

object FlatMap {
  type Aux[F[_], Tag0] = FlatMap[F] { type Tag = Tag0 }

  def apply[F[_]](implicit F: FlatMap[F]): FlatMap[F] = F
}