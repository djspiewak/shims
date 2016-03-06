package shims

trait Functor[F[_]] {
  type Tag

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {
  type Aux[F[_], Tag0] = Functor[F] { type Tag = Tag0 }

  def apply[F[_]](implicit F: Functor[F]): Functor[F] = F
}