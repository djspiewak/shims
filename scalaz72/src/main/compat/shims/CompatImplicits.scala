package shims

import shims.util.=/=

private[shims] trait LowPriorityImplicits2Variants {

  def bindrec1[F[_]](implicit F: _root_.scalaz.BindRec[F]): FlatMapRec.Aux[F, Synthetic]

  implicit def bindrec2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.BindRec[F2[Z, ?]]): FlatMapRec.Aux[F2[Z, ?], Synthetic] = bindrec1[F2[Z, ?]]
  implicit def bindrec3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.BindRec[F2[Y, Z, ?]]): FlatMapRec.Aux[F2[Y, Z, ?], Synthetic] = bindrec1[F2[Y, Z, ?]]

  implicit def bindrecH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.BindRec[F[G, ?]]): FlatMapRec.Aux[F[G, ?], Synthetic] = bindrec1[F[G, ?]]
  implicit def bindrecH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.BindRec[F2[G, Z, ?]]): FlatMapRec.Aux[F2[G, Z, ?], Synthetic] = bindrec1[F2[G, Z, ?]]
  implicit def bindrecH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.BindRec[F2[G, Y, Z, ?]]): FlatMapRec.Aux[F2[G, Y, Z, ?], Synthetic] = bindrec1[F2[G, Y, Z, ?]]

  def rbindrec1[F[_], Tag](implicit F: FlatMapRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F]

  implicit def rbindrec2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: FlatMapRec.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F2[Z, ?]] = rbindrec1[F2[Z, ?], Tag]
  implicit def rbindrec3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: FlatMapRec.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F2[Y, Z, ?]] = rbindrec1[F2[Y, Z, ?], Tag]

  implicit def rbindrecH1[F[_[_], _], G[_], Tag](implicit F: FlatMapRec.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F[G, ?]] = rbindrec1[F[G, ?], Tag]
  implicit def rbindrecH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: FlatMapRec.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F2[G, Z, ?]] = rbindrec1[F2[G, Z, ?], Tag]
  implicit def rbindrecH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: FlatMapRec.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F2[G, Y, Z, ?]] = rbindrec1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 with LowPriorityImplicits2Variants {

  implicit def bindrec1[F[_]](implicit F: _root_.scalaz.BindRec[F]): FlatMapRec.Aux[F, Synthetic] = new FlatMapRec[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
    def tailRecM[A, B](a: A)(f: (A) => F[Either[A, B]]): F[B] =
      F.tailrecM[A, B]((ia: A) => map(f(ia))(_root_.scalaz.\/.fromEither))(a)
  }

  implicit def rbindrec1[F[_], Tag](implicit F: FlatMapRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.BindRec[F] = new _root_.scalaz.BindRec[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def bind[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
    def tailrecM[A, B](f: A => F[_root_.scalaz.\/[A, B]])(a: A): F[B] = F.tailRecM(a)(ia => map(f(ia))(_.toEither))
  }
}
