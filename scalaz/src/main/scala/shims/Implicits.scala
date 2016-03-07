// scalaz
package shims

import shims.util.=/=

private[shims] trait Synthetic

private[shims] trait LowPriorityImplicits4 {

  private type SynthFunctor[F[_]] = Functor.Aux[F, Synthetic]

  implicit def functor1[F[_]](implicit F: _root_.scalaz.Functor[F]): SynthFunctor[F] = new Functor[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functor2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Functor[F2[Z, ?]]): SynthFunctor[F2[Z, ?]] = functor1[F2[Z, ?]]
  implicit def functor3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Functor[F2[Y, Z, ?]]): SynthFunctor[F2[Y, Z, ?]] = functor1[F2[Y, Z, ?]]

  implicit def functorH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Functor[F[G, ?]]): SynthFunctor[F[G, ?]] = functor1[F[G, ?]]
  implicit def functorH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Functor[F2[G, Z, ?]]): SynthFunctor[F2[G, Z, ?]] = functor1[F2[G, Z, ?]]
  implicit def functorH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Functor[F2[G, Y, Z, ?]]): SynthFunctor[F2[G, Y, Z, ?]] = functor1[F2[G, Y, Z, ?]]

  // TODO currently no rtraverse due to the tighter constraints of Cats' Traverse.  can probably implement once we have Foldable
}

private[shims] trait LowPriorityImplicits3 extends LowPriorityImplicits4 {

  implicit def applicative1[F[_]](implicit F: _root_.scalaz.Applicative[F]): Applicative.Aux[F, Synthetic] = new Applicative[F] {
    type Tag = Synthetic

    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = F.ap(fa)(f)
  }

  implicit def applicative2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Applicative[F2[Z, ?]]): Applicative.Aux[F2[Z, ?], Synthetic] = applicative1[F2[Z, ?]]
  implicit def applicative3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Applicative[F2[Y, Z, ?]]): Applicative.Aux[F2[Y, Z, ?], Synthetic] = applicative1[F2[Y, Z, ?]]

  implicit def applicativeH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Applicative[F[G, ?]]): Applicative.Aux[F[G, ?], Synthetic] = applicative1[F[G, ?]]
  implicit def applicativeH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Applicative[F2[G, Z, ?]]): Applicative.Aux[F2[G, Z, ?], Synthetic] = applicative1[F2[G, Z, ?]]
  implicit def applicativeH3[F[_[_], _, _, _], F2[_[_],_,  _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Applicative[F2[G, Y, Z, ?]]): Applicative.Aux[F2[G, Y, Z, ?], Synthetic] = applicative1[F2[G, Y, Z, ?]]

  implicit def rmonad1[F[_], Tag](implicit F: Monad.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F] = new _root_.scalaz.Monad[F] {
    def point[A](a: => A): F[A] = F.point(a)
    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def bind[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
  }

  implicit def rmonad2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: Monad.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F2[Z, ?]] = rmonad1[F2[Z, ?], Tag]
  implicit def rmonad3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: Monad.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F2[Y, Z, ?]] = rmonad1[F2[Y, Z, ?], Tag]

  implicit def rmonadH1[F[_[_], _], G[_], Tag](implicit F: Monad.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F[G, ?]] = rmonad1[F[G, ?], Tag]
  implicit def rmonadH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: Monad.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F2[G, Z, ?]] = rmonad1[F2[G, Z, ?], Tag]
  implicit def rmonadH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: Monad.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monad[F2[G, Y, Z, ?]] = rmonad1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def bind1[F[_]](implicit F: _root_.scalaz.Bind[F]): FlatMap.Aux[F, Synthetic] = new FlatMap[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }

  implicit def bind2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Bind[F2[Z, ?]]): FlatMap.Aux[F2[Z, ?], Synthetic] = bind1[F2[Z, ?]]
  implicit def bind3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Bind[F2[Y, Z, ?]]): FlatMap.Aux[F2[Y, Z, ?], Synthetic] = bind1[F2[Y, Z, ?]]

  implicit def bindH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Bind[F[G, ?]]): FlatMap.Aux[F[G, ?], Synthetic] = bind1[F[G, ?]]
  implicit def bindH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Bind[F2[G, Z, ?]]): FlatMap.Aux[F2[G, Z, ?], Synthetic] = bind1[F2[G, Z, ?]]
  implicit def bindH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Bind[F2[G, Y, Z, ?]]): FlatMap.Aux[F2[G, Y, Z, ?], Synthetic] = bind1[F2[G, Y, Z, ?]]

  implicit def rbind1[F[_], Tag](implicit F: FlatMap.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F] = new _root_.scalaz.Bind[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def bind[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
  }

  implicit def rbind2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: FlatMap.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F2[Z, ?]] = rbind1[F2[Z, ?], Tag]
  implicit def rbind3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: FlatMap.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F2[Y, Z, ?]] = rbind1[F2[Y, Z, ?], Tag]

  implicit def rbindH1[F[_[_], _], G[_], Tag](implicit F: FlatMap.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F[G, ?]] = rbind1[F[G, ?], Tag]
  implicit def rbindH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: FlatMap.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F2[G, Z, ?]] = rbind1[F2[G, Z, ?], Tag]
  implicit def rbindH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: FlatMap.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Bind[F2[G, Y, Z, ?]] = rbind1[F2[G, Y, Z, ?], Tag]
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.scalaz.Monad[F]): Monad.Aux[F, Synthetic] = new Monad[F] {
    type Tag = Synthetic

    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }

  implicit def monad2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Monad[F2[Z, ?]]): Monad.Aux[F2[Z, ?], Synthetic] = monad1[F2[Z, ?]]
  implicit def monad3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Monad[F2[Y, Z, ?]]): Monad.Aux[F2[Y, Z, ?], Synthetic] = monad1[F2[Y, Z, ?]]

  implicit def monadH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Monad[F[G, ?]]): Monad.Aux[F[G, ?], Synthetic] = monad1[F[G, ?]]
  implicit def monadH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Monad[F2[G, Z, ?]]): Monad.Aux[F2[G, Z, ?], Synthetic] = monad1[F2[G, Z, ?]]
  implicit def monadH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Monad[F2[G, Y, Z, ?]]): Monad.Aux[F2[G, Y, Z, ?], Synthetic] = monad1[F2[G, Y, Z, ?]]

  implicit def rapplicative1[F[_], Tag](implicit F: Applicative.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F] = new _root_.scalaz.Applicative[F] {
    def point[A](a: => A): F[A] = F.point(a)
    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: => F[A])(f: => F[A => B]): F[B] = F.ap(fa)(f)
  }

  implicit def rapplicative2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: Applicative.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F2[Z, ?]] = rapplicative1[F2[Z, ?], Tag]
  implicit def rapplicative3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: Applicative.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F2[Y, Z, ?]] = rapplicative1[F2[Y, Z, ?], Tag]

  implicit def rapplicativeH1[F[_[_], _], G[_], Tag](implicit F: Applicative.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F[G, ?]] = rapplicative1[F[G, ?], Tag]
  implicit def rapplicativeH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: Applicative.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F2[G, Z, ?]] = rapplicative1[F2[G, Z, ?], Tag]
  implicit def rapplicativeH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: Applicative.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Applicative[F2[G, Y, Z, ?]] = rapplicative1[F2[G, Y, Z, ?], Tag]
}

/*
 * Priority is inverted on the *reverse* transformations to avoid diverging implicit expansion.  It's still sound so
 * long as priority on providers is correct (which it is here).
 */
trait Implicits extends LowPriorityImplicits1 {

  implicit def kleisli: KleisliLike[_root_.scalaz.Kleisli] = new KleisliLike[_root_.scalaz.Kleisli] {
    def apply[M[_], A, B](run: A => M[B]): _root_.scalaz.Kleisli[M, A, B] = _root_.scalaz.Kleisli(run)
  }

  implicit def monoid[A](implicit A: _root_.scalaz.Monoid[A]): Monoid.Aux[A, Synthetic] = new Monoid[A] {
    type Tag = Synthetic

    def zero: A = A.zero
    def append(a1: A, a2: => A): A = A.append(a1, a2)
  }

  implicit def rmonoid[A, Tag](implicit A: Monoid.Aux[A, Tag], neg: Tag =/= Synthetic): _root_.scalaz.Monoid[A] = new _root_.scalaz.Monoid[A] {
    def zero = A.zero
    def append(a1: A, a2: => A) = A.append(a1, a2)
  }

  implicit def traverse1[F[_]](implicit F: _root_.scalaz.Traverse[F]): Traverse.Aux[F, Synthetic] = new Traverse[F] {
    type Tag = Synthetic

    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: Applicative[G]): G[F[B]] = {
      val cap: _root_.scalaz.Applicative[G] = new _root_.scalaz.Applicative[G] {
        def point[A](a: => A): G[A] = G.point(a)
        override def map[A, B](ga: G[A])(f: A => B): G[B] = G.map(ga)(f)
        def product[A, B](ga: G[A], gb: G[B]): G[(A, B)] = ap(ga)(map(gb) { b => { a: A => (a, b) } })
        def ap[A, B](ga: => G[A])(f: => G[A => B]): G[B] = G.ap(ga)(f)
      }

      F.traverse(fa)(f)(cap)
    }
  }

  implicit def traverse2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Traverse[F2[Z, ?]]): Traverse.Aux[F2[Z, ?], Synthetic] = traverse1[F2[Z, ?]]
  implicit def traverse3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Traverse[F2[Y, Z, ?]]): Traverse.Aux[F2[Y, Z, ?], Synthetic] = traverse1[F2[Y, Z, ?]]

  implicit def traverseH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Traverse[F[G, ?]]): Traverse.Aux[F[G, ?], Synthetic] = traverse1[F[G, ?]]
  implicit def traverseH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Traverse[F2[G, Z, ?]]): Traverse.Aux[F2[G, Z, ?], Synthetic] = traverse1[F2[G, Z, ?]]
  implicit def traverseH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Traverse[F2[G, Y, Z, ?]]): Traverse.Aux[F2[G, Y, Z, ?], Synthetic] = traverse1[F2[G, Y, Z, ?]]

  implicit def rfunctor1[F[_], Tag](implicit F: Functor.Aux[F, Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F] = new _root_.scalaz.Functor[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def rfunctor2[F[_, _], F2[_, _], Z, Tag](implicit ev: Permute2[F, F2], F: Functor.Aux[F2[Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F2[Z, ?]] = rfunctor1[F2[Z, ?], Tag]
  implicit def rfunctor3[F[_, _, _], F2[_, _, _], Y, Z, Tag](implicit ev: Permute3[F, F2], F: Functor.Aux[F2[Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F2[Y, Z, ?]] = rfunctor1[F2[Y, Z, ?], Tag]

  implicit def rfunctorH1[F[_[_], _], G[_], Tag](implicit F: Functor.Aux[F[G, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F[G, ?]] = rfunctor1[F[G, ?], Tag]
  implicit def rfunctorH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z, Tag](implicit ev: PermuteH2[F, F2], F: Functor.Aux[F2[G, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F2[G, Z, ?]] = rfunctor1[F2[G, Z, ?], Tag]
  implicit def rfunctorH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z, Tag](implicit ev: PermuteH3[F, F2], F: Functor.Aux[F2[G, Y, Z, ?], Tag], neg: Tag =/= Synthetic): _root_.scalaz.Functor[F2[G, Y, Z, ?]] = rfunctor1[F2[G, Y, Z, ?], Tag]
}