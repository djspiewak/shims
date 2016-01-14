package shims

trait Traverse[F[_]] extends Functor[F] {
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
}

object Traverse {
  def apply[F[_]](implicit F: Traverse[F]): Traverse[F] = F
}