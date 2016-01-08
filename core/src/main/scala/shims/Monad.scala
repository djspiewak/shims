package shims

trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = flatMap(f)(map(fa))
}