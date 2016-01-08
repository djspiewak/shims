// scalaz
package shims

private[shims] trait LowPriorityImplicits3 {

  implicit def functor1[F[_]](implicit F: _root_.scalaz.Functor[F]): Functor[F] = new Functor[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def applicative1[F[_]](implicit F: _root_.scalaz.Applicative[F]): Applicative[F] = new Applicative[F] {
    def point[A](a: A): F[A] = F.point(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = F.ap(fa)(f)
  }

  implicit def flatMap1[F[_]](implicit F: _root_.scalaz.Bind[F]): FlatMap[F] = new FlatMap[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.scalaz.Monad[F]): Monad[F] = new Monad[F] {
    def point[A](a: A): F[A] = F.point(a)
    def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)
  }
}

trait Implicits extends LowPriorityImplicits1 {

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
}