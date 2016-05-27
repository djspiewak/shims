// cats
package shims

private[shims] trait LowPriorityImplicits3 {

  implicit def functor1[F[_]](implicit F: _root_.cats.Functor[F]): Functor[F] = new Functor[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def applicative1[F[_]](implicit F: _root_.cats.Applicative[F]): Applicative[F] = new Applicative[F] {
    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = F.ap(f)(fa)
  }

  implicit def flatMap1[F[_]](implicit F: _root_.cats.FlatMap[F]): FlatMap[F] = new FlatMap[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
  }
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.cats.Monad[F]): Monad[F] = new Monad[F] {
    def point[A](a: A): F[A] = F.pure(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)
  }
}

trait Implicits extends LowPriorityImplicits1 {

  implicit def traverse1[F[_]](implicit F: _root_.cats.Traverse[F]): Traverse[F] = new Traverse[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)

    def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: Applicative[G]): G[F[B]] = {
      val cap: _root_.cats.Applicative[G] = new _root_.cats.Applicative[G] {
        def pure[A](a: A): G[A] = G.point(a)
        def map[A, B](ga: G[A])(f: A => B): G[B] = G.map(ga)(f)
        def product[A, B](ga: G[A], gb: G[B]): G[(A, B)] = ap(map(gb) { b => { a: A => (a, b) } })(ga)
        def ap[A, B](f: G[A => B])(ga: G[A]): G[B] = G.ap(ga)(f)
      }

      F.traverse(fa)(f)(cap)
    }
  }
}