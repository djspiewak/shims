package shims

trait MonadRec[F[_]] extends Monad[F] with FlatMapRec[F]

object MonadRec {
  type Aux[F[_], Tag0] = MonadRec[F] { type Tag = Tag0 }

  def apply[F[_]](implicit F: MonadRec[F]): MonadRec[F] = F
}
