package shims

trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = flatMap(f)(map(fa))
}

object Monad {
  type Aux[F[_], Tag0] = Monad[F] { type Tag = Tag0 }

  def apply[F[_]](implicit F: Monad[F]): Monad[F] = F
}