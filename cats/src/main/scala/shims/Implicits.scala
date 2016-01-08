// cats
package shims

private[shims] trait LowPriorityImplicits3 {

  implicit def functor1[F[_]](implicit F: _root_.cats.Functor[F]): shims.Functor[F] = ???
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def applicative1[F[_]](implicit F: _root_.cats.Applicative[F]): shims.Applicative[F] = ???

  implicit def flatMap1[F[_]](implicit F: _root_.cats.FlatMap[F]): shims.FlatMap[F] = ???
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.cats.Monad[F]): shims.Monad[F] = ???
}

trait Implicits extends LowPriorityImplicits1 {

  implicit def traverse1[F[_]](implicit F: _root_.cats.Traverse[F]): shims.Traverse[F] = ???
}