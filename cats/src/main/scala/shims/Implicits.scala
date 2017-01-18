// cats
package shims

import shims.util.=/=

private[shims] trait Synthetic

private[shims] trait LowPriorityImplicits4Variants {

  private type SynthFunctor[F[_]] = Functor.Aux[F, Synthetic]

  def functor1[F[_]](implicit F: _root_.cats.Functor[F]): SynthFunctor[F]

  implicit def functor2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.cats.Functor[F2[Z, ?]]): SynthFunctor[F2[Z, ?]] = functor1[F2[Z, ?]]
  implicit def functor3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.cats.Functor[F2[Y, Z, ?]]): SynthFunctor[F2[Y, Z, ?]] = functor1[F2[Y, Z, ?]]

  implicit def functorH1[F[_[_], _], G[_]](implicit F: _root_.cats.Functor[F[G, ?]]): SynthFunctor[F[G, ?]] = functor1[F[G, ?]]
  implicit def functorH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.cats.Functor[F2[G, Z, ?]]): SynthFunctor[F2[G, Z, ?]] = functor1[F2[G, Z, ?]]
  implicit def functorH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.cats.Functor[F2[G, Y, Z, ?]]): SynthFunctor[F2[G, Y, Z, ?]] = functor1[F2[G, Y, Z, ?]]

  // TODO currently no rtraverse due to the tighter constraints of Cats' Traverse.  can probably implement once we have Foldable
}

private[shims] trait LowPriorityImplicits4 extends LowPriorityImplicits4Variants {

  implicit def functor1[F[_]](implicit F: _root_.cats.Functor[F]): Functor.Aux[F, Synthetic] = new Functor[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }
}

private[shims] trait LowPriorityImplicits3Variants {

  def applicative1[F[_]](implicit F: _root_.cats.Applicative[F]): Applicative.Aux[F, Synthetic]

  implicit def applicative2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.cats.Applicative[F2[Z, ?]]): Applicative.Aux[F2[Z, ?], Synthetic] = applicative1[F2[Z, ?]]
  implicit def applicative3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.cats.Applicative[F2[Y, Z, ?]]): Applicative.Aux[F2[Y, Z, ?], Synthetic] = applicative1[F2[Y, Z, ?]]

  implicit def applicativeH1[F[_[_], _], G[_]](implicit F: _root_.cats.Applicative[F[G, ?]]): Applicative.Aux[F[G, ?], Synthetic] = applicative1[F[G, ?]]
  implicit def applicativeH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.cats.Applicative[F2[G, Z, ?]]): Applicative.Aux[F2[G, Z, ?], Synthetic] = applicative1[F2[G, Z, ?]]
  implicit def applicativeH3[F[_[_], _, _, _], F2[_[_],_,  _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.cats.Applicative[F2[G, Y, Z, ?]]): Applicative.Aux[F2[G, Y, Z, ?], Synthetic] = applicative1[F2[G, Y, Z, ?]]

  def rmonad1[F[_], Tag](implicit F: MonadRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F]

  implicit def rmonad2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: MonadRec.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F2[Z, ?]] = rmonad1[F2[Z, ?], Tag]
  implicit def rmonad3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: MonadRec.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F2[Y, Z, ?]] = rmonad1[F2[Y, Z, ?], Tag]

  implicit def rmonadH1[F[_[_], _], G[_], Tag](implicit F: MonadRec.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F[G, ?]] = rmonad1[F[G, ?], Tag]
  implicit def rmonadH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: MonadRec.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F2[G, Z, ?]] = rmonad1[F2[G, Z, ?], Tag]
  implicit def rmonadH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: MonadRec.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F2[G, Y, Z, ?]] = rmonad1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits3 extends LowPriorityImplicits4 with LowPriorityImplicits3Variants {

  implicit def applicative1[F[_]](implicit F: _root_.cats.Applicative[F]): Applicative.Aux[F, Synthetic] = new Applicative[F] {
    type Tag = Synthetic

    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = F.ap(f)(fa)
  }

  implicit def rmonad1[F[_], Tag](implicit F: MonadRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Monad[F] = new _root_.cats.Monad[F] {
    def pure[A](a: A): F[A] = F.point(a)
    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
    override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = F.tailRecM(a)(f)
  }
}

private[shims] trait LowPriorityImplicits2Variants {

  def flatMap1[F[_]](implicit F: _root_.cats.FlatMap[F]): FlatMapRec.Aux[F, Synthetic]

  implicit def flatMap2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.cats.FlatMap[F2[Z, ?]]): FlatMapRec.Aux[F2[Z, ?], Synthetic] = flatMap1[F2[Z, ?]]
  implicit def flatMap3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.cats.FlatMap[F2[Y, Z, ?]]): FlatMapRec.Aux[F2[Y, Z, ?], Synthetic] = flatMap1[F2[Y, Z, ?]]

  implicit def flatMapH1[F[_[_], _], G[_]](implicit F: _root_.cats.FlatMap[F[G, ?]]): FlatMapRec.Aux[F[G, ?], Synthetic] = flatMap1[F[G, ?]]
  implicit def flatMapH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.cats.FlatMap[F2[G, Z, ?]]): FlatMapRec.Aux[F2[G, Z, ?], Synthetic] = flatMap1[F2[G, Z, ?]]
  implicit def flatMapH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.cats.FlatMap[F2[G, Y, Z, ?]]): FlatMapRec.Aux[F2[G, Y, Z, ?], Synthetic] = flatMap1[F2[G, Y, Z, ?]]

  def rflatMap1[F[_], Tag](implicit F: FlatMapRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F]

  implicit def rflatMap2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: FlatMapRec.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F2[Z, ?]] = rflatMap1[F2[Z, ?], Tag]
  implicit def rflatMap3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: FlatMapRec.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F2[Y, Z, ?]] = rflatMap1[F2[Y, Z, ?], Tag]

  implicit def rflatMapH1[F[_[_], _], G[_], Tag](implicit F: FlatMapRec.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F[G, ?]] = rflatMap1[F[G, ?], Tag]
  implicit def rflatMapH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: FlatMapRec.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F2[G, Z, ?]] = rflatMap1[F2[G, Z, ?], Tag]
  implicit def rflatMapH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: FlatMapRec.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F2[G, Y, Z, ?]] = rflatMap1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 with LowPriorityImplicits2Variants {

  implicit def flatMap1[F[_]](implicit F: _root_.cats.FlatMap[F]): FlatMapRec.Aux[F, Synthetic] = new FlatMapRec[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: (A) => F[Either[A, B]]): F[B] = F.tailRecM(a)(f)
  }

  implicit def rflatMap1[F[_], Tag](implicit F: FlatMapRec.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.FlatMap[F] = new _root_.cats.FlatMap[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = F.tailRecM(a)(f)
  }
}

private[shims] trait LowPriorityImplicits1Variants {

  def monad1[F[_]](implicit F: _root_.cats.Monad[F]): Monad.Aux[F, Synthetic]

  implicit def monad2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.cats.Monad[F2[Z, ?]]): Monad.Aux[F2[Z, ?], Synthetic] = monad1[F2[Z, ?]]
  implicit def monad3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.cats.Monad[F2[Y, Z, ?]]): Monad.Aux[F2[Y, Z, ?], Synthetic] = monad1[F2[Y, Z, ?]]

  implicit def monadH1[F[_[_], _], G[_]](implicit F: _root_.cats.Monad[F[G, ?]]): Monad.Aux[F[G, ?], Synthetic] = monad1[F[G, ?]]
  implicit def monadH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.cats.Monad[F2[G, Z, ?]]): Monad.Aux[F2[G, Z, ?], Synthetic] = monad1[F2[G, Z, ?]]
  implicit def monadH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.cats.Monad[F2[G, Y, Z, ?]]): Monad.Aux[F2[G, Y, Z, ?], Synthetic] = monad1[F2[G, Y, Z, ?]]

  def rapplicative1[F[_], Tag](implicit F: Applicative.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F]

  implicit def rapplicative2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: Applicative.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F2[Z, ?]] = rapplicative1[F2[Z, ?], Tag]
  implicit def rapplicative3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: Applicative.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F2[Y, Z, ?]] = rapplicative1[F2[Y, Z, ?], Tag]

  implicit def rapplicativeH1[F[_[_], _], G[_], Tag](implicit F: Applicative.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F[G, ?]] = rapplicative1[F[G, ?], Tag]
  implicit def rapplicativeH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: Applicative.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F2[G, Z, ?]] = rapplicative1[F2[G, Z, ?], Tag]
  implicit def rapplicativeH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: Applicative.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F2[G, Y, Z, ?]] = rapplicative1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 with LowPriorityImplicits1Variants {

  implicit def equal[A](implicit A: _root_.cats.Eq[A]): Equal.Aux[A, Synthetic] = new Equal[A] {
    type Tag = Synthetic

    def equal(a1: A, a2: A) = A.eqv(a1, a2)
  }

  implicit def rorder[A, Tag](implicit A: Order.Aux[A, Tag], neg: Tag =/= Synthetic): _root_.cats.Order[A] = new _root_.cats.Order[A] {
    override def eqv(a1: A, a2: A) = A.equal(a1, a2)
    def compare(a1: A, a2: A) = A.order(a1, a2)
  }

  implicit def monad1[F[_]](implicit F: _root_.cats.Monad[F]): Monad.Aux[F, Synthetic] = new Monad[F] {
    type Tag = Synthetic

    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
  }

  implicit def rapplicative1[F[_], Tag](implicit F: Applicative.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Applicative[F] = new _root_.cats.Applicative[F] {
    def pure[A](a: A): F[A] = F.point(a)
    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = F.ap(fb)(F.map(fa) { a => { b: B => (a, b) } })
    def ap[A, B](f: F[A => B])(fa: F[A]): F[B] = F.ap(fa)(f)
  }
}

private[shims] trait ImplicitVariants {

  def traverse1[F[_]](implicit F: _root_.cats.Traverse[F]): Traverse.Aux[F, Synthetic]

  implicit def traverse2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.cats.Traverse[F2[Z, ?]]): Traverse.Aux[F2[Z, ?], Synthetic] = traverse1[F2[Z, ?]]
  implicit def traverse3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.cats.Traverse[F2[Y, Z, ?]]): Traverse.Aux[F2[Y, Z, ?], Synthetic] = traverse1[F2[Y, Z, ?]]

  implicit def traverseH1[F[_[_], _], G[_]](implicit F: _root_.cats.Traverse[F[G, ?]]): Traverse.Aux[F[G, ?], Synthetic] = traverse1[F[G, ?]]
  implicit def traverseH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.cats.Traverse[F2[G, Z, ?]]): Traverse.Aux[F2[G, Z, ?], Synthetic] = traverse1[F2[G, Z, ?]]
  implicit def traverseH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.cats.Traverse[F2[G, Y, Z, ?]]): Traverse.Aux[F2[G, Y, Z, ?], Synthetic] = traverse1[F2[G, Y, Z, ?]]

  def rfunctor1[F[_], Tag](implicit F: Functor.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F]

  implicit def rfunctor2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: Functor.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F2[Z, ?]] = rfunctor1[F2[Z, ?], Tag]
  implicit def rfunctor3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: Functor.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F2[Y, Z, ?]] = rfunctor1[F2[Y, Z, ?], Tag]

  implicit def rfunctorH1[F[_[_], _], G[_], Tag](implicit F: Functor.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F[G, ?]] = rfunctor1[F[G, ?], Tag]
  implicit def rfunctorH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: Functor.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F2[G, Z, ?]] = rfunctor1[F2[G, Z, ?], Tag]
  implicit def rfunctorH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: Functor.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F2[G, Y, Z, ?]] = rfunctor1[F2[G, Y, Z, ?], Tag]
}

/*
 * Priority is inverted on the *reverse* transformations to avoid diverging implicit expansion.  It's still sound so
 * long as priority on providers is correct (which it is here).  -Ypartial-unification is supported by deprioritizing
 * the shape alternates, and its absence is supported with appropriate priority by retaining them in the variant traits.
 */
trait Implicits extends LowPriorityImplicits1 with ImplicitVariants {

  implicit def kleisli: KleisliLike[_root_.cats.data.Kleisli] = new KleisliLike[_root_.cats.data.Kleisli] {
    def apply[M[_], A, B](run: A => M[B]): _root_.cats.data.Kleisli[M, A, B] = _root_.cats.data.Kleisli(run)
  }

  implicit def monoid[A](implicit A: _root_.cats.Monoid[A]): Monoid.Aux[A, Synthetic] = new Monoid[A] {
    type Tag = Synthetic

    def zero: A = A.empty
    def append(a1: A, a2: => A): A = A.combine(a1, a2)
  }

  implicit def rmonoid[A, Tag](implicit A: Monoid.Aux[A, Tag], neg: Tag =/= Synthetic): _root_.cats.Monoid[A] = new _root_.cats.Monoid[A] {
    def empty = A.zero
    def combine(a1: A, a2: A) = A.append(a1, a2)
  }

  implicit def show[A](implicit A: _root_.cats.Show[A]): Show.Aux[A, Synthetic] = new Show[A] {
    type Tag = Synthetic

    def show(a: A) = A.show(a)
  }

  implicit def rshow[A, Tag](implicit A: Show.Aux[A, Tag], neg: Tag =/= Synthetic): _root_.cats.Show[A] = new _root_.cats.Show[A] {
    def show(a: A) = A.show(a)
  }

  implicit def order[A](implicit A: _root_.cats.Order[A]): Order.Aux[A, Synthetic] = new Order[A] {
    type Tag = Synthetic

    def equal(a1: A, a2: A) = A.eqv(a1, a2)
    def order(a1: A, a2: A) = A.compare(a1, a2)
  }

  implicit def requal[A, Tag](implicit A: Equal.Aux[A, Tag], neg: Tag =/= Synthetic): _root_.cats.Eq[A] = new _root_.cats.Eq[A] {
    def eqv(a1: A, a2: A) = A.equal(a1, a2)
  }

  implicit def traverse1[F[_]](implicit F: _root_.cats.Traverse[F]): Traverse.Aux[F, Synthetic] = new Traverse[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: Applicative[G]): G[F[B]] = {
      val cap: _root_.cats.Applicative[G] = new _root_.cats.Applicative[G] {
        def pure[A](a: A): G[A] = G.point(a)
        override def map[A, B](ga: G[A])(f: A => B): G[B] = G.map(ga)(f)
        override def product[A, B](ga: G[A], gb: G[B]): G[(A, B)] = ap(map(gb) { b => { a: A => (a, b) } })(ga)
        def ap[A, B](f: G[A => B])(ga: G[A]): G[B] = G.ap(ga)(f)
      }

      F.traverse(fa)(f)(cap)
    }
  }

  implicit def rfunctor1[F[_], Tag](implicit F: Functor.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.cats.Functor[F] = new _root_.cats.Functor[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }
}
