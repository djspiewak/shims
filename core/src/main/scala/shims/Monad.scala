package shims

trait Monad[F[_]] extends FlatMap[F] with Applicative[F]