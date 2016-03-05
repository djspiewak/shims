// scalaz
package shims

private[shims] trait LowPriorityImplicits3 {

  implicit def functor1[F[_]](implicit F: _root_.scalaz.Functor[F]): Functor[F] = new Functor[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functor2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Functor[F2[Z, ?]]): Functor[F2[Z, ?]] = functor1[F2[Z, ?]]
  implicit def functor3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Functor[F2[Y, Z, ?]]): Functor[F2[Y, Z, ?]] = functor1[F2[Y, Z, ?]]

  implicit def functorH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Functor[F[G, ?]]): Functor[F[G, ?]] = functor1[F[G, ?]]
  implicit def functorH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Functor[F2[G, Z, ?]]): Functor[F2[G, Z, ?]] = functor1[F2[G, Z, ?]]
  implicit def functorH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Functor[F2[G, Y, Z, ?]]): Functor[F2[G, Y, Z, ?]] = functor1[F2[G, Y, Z, ?]]
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def applicative1[F[_]](implicit F: _root_.scalaz.Applicative[F]): Applicative[F] = new Applicative[F] {
    def point[A](a: A): F[A] = F.point(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = F.ap(fa)(f)
  }

  implicit def applicative2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Applicative[F2[Z, ?]]): Applicative[F2[Z, ?]] = applicative1[F2[Z, ?]]
  implicit def applicative3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Applicative[F2[Y, Z, ?]]): Applicative[F2[Y, Z, ?]] = applicative1[F2[Y, Z, ?]]

  implicit def applicativeH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Applicative[F[G, ?]]): Applicative[F[G, ?]] = applicative1[F[G, ?]]
  implicit def applicativeH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Applicative[F2[G, Z, ?]]): Applicative[F2[G, Z, ?]] = applicative1[F2[G, Z, ?]]
  implicit def applicativeH3[F[_[_], _, _, _], F2[_[_],_,  _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Applicative[F2[G, Y, Z, ?]]): Applicative[F2[G, Y, Z, ?]] = applicative1[F2[G, Y, Z, ?]]

  implicit def flatMap1[F[_]](implicit F: _root_.scalaz.Bind[F]): FlatMap[F] = new FlatMap[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }

  implicit def flatMap2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Bind[F2[Z, ?]]): FlatMap[F2[Z, ?]] = flatMap1[F2[Z, ?]]
  implicit def flatMap3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Bind[F2[Y, Z, ?]]): FlatMap[F2[Y, Z, ?]] = flatMap1[F2[Y, Z, ?]]

  implicit def flatMapH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Bind[F[G, ?]]): FlatMap[F[G, ?]] = flatMap1[F[G, ?]]
  implicit def flatMapH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Bind[F2[G, Z, ?]]): FlatMap[F2[G, Z, ?]] = flatMap1[F2[G, Z, ?]]
  implicit def flatMapH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Bind[F2[G, Y, Z, ?]]): FlatMap[F2[G, Y, Z, ?]] = flatMap1[F2[G, Y, Z, ?]]
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.scalaz.Monad[F]): Monad[F] = new Monad[F] {
    def point[A](a: A): F[A] = F.point(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }

  implicit def monad2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Monad[F2[Z, ?]]): Monad[F2[Z, ?]] = monad1[F2[Z, ?]]
  implicit def monad3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Monad[F2[Y, Z, ?]]): Monad[F2[Y, Z, ?]] = monad1[F2[Y, Z, ?]]

  implicit def monadH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Monad[F[G, ?]]): Monad[F[G, ?]] = monad1[F[G, ?]]
  implicit def monadH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Monad[F2[G, Z, ?]]): Monad[F2[G, Z, ?]] = monad1[F2[G, Z, ?]]
  implicit def monadH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Monad[F2[G, Y, Z, ?]]): Monad[F2[G, Y, Z, ?]] = monad1[F2[G, Y, Z, ?]]
}

trait Implicits extends LowPriorityImplicits1 {

  implicit def kleisli: KleisliLike[_root_.scalaz.Kleisli] = new KleisliLike[_root_.scalaz.Kleisli] {
    def apply[M[_], A, B](run: A => M[B]): _root_.scalaz.Kleisli[M, A, B] = _root_.scalaz.Kleisli(run)
  }

  implicit def traverse1[F[_]](implicit F: _root_.scalaz.Traverse[F]): Traverse[F] = new Traverse[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: Applicative[G]): G[F[B]] = {
      val cap: _root_.scalaz.Applicative[G] = new _root_.scalaz.Applicative[G] {
        def point[A](a: => A): G[A] = G.point(a)
        def ap[A, B](ga: => G[A])(f: => G[A => B]): G[B] = G.ap(ga)(f)
      }

      F.traverse(fa)(f)(cap)
    }
  }

  implicit def traverse2[F[_, _], F2[_, _], Z](implicit ev: Permute2[F, F2], F: _root_.scalaz.Traverse[F2[Z, ?]]): Traverse[F2[Z, ?]] = traverse1[F2[Z, ?]]
  implicit def traverse3[F[_, _, _], F2[_, _, _], Y, Z](implicit ev: Permute3[F, F2], F: _root_.scalaz.Traverse[F2[Y, Z, ?]]): Traverse[F2[Y, Z, ?]] = traverse1[F2[Y, Z, ?]]

  implicit def traverseH1[F[_[_], _], G[_]](implicit F: _root_.scalaz.Traverse[F[G, ?]]): Traverse[F[G, ?]] = traverse1[F[G, ?]]
  implicit def traverseH2[F[_[_], _, _], F2[_[_], _, _], G[_], Z](implicit ev: PermuteH2[F, F2], F: _root_.scalaz.Traverse[F2[G, Z, ?]]): Traverse[F2[G, Z, ?]] = traverse1[F2[G, Z, ?]]
  implicit def traverseH3[F[_[_], _, _, _], F2[_[_], _, _, _], G[_], Y, Z](implicit ev: PermuteH3[F, F2], F: _root_.scalaz.Traverse[F2[G, Y, Z, ?]]): Traverse[F2[G, Y, Z, ?]] = traverse1[F2[G, Y, Z, ?]]
}