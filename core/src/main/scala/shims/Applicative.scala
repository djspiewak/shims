package shims

trait Applicative[F[_]] extends Functor[F] {
  def point[A](a: A): F[A]
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
}

object Applicative {
  def apply[F[_]](implicit F: Applicative[F]): Applicative[F] = F
}