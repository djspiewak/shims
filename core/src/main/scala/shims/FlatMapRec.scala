package shims

trait FlatMapRec[F[_]] extends FlatMap[F] {
  def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B]
}

object FlatMapRec {
  type Aux[F[_], Tag0] = FlatMapRec[F] { type Tag = Tag0 }

  def apply[F[_]](implicit F: FlatMapRec[F]): FlatMapRec[F] = F
}
