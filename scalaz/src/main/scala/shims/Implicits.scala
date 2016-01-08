// scalaz
package shims

private[shims] trait LowPriorityImplicits3 {

  implicit def functor1[F[_]](implicit F: _root_.scalaz.Functor[F]): shims.Functor[F] = ???
}

private[shims] trait LowPriorityImplicits2 extends LowPriorityImplicits3 {

  implicit def applicative1[F[_]](implicit F: _root_.scalaz.Applicative[F]): shims.Applicative[F] = ???

  implicit def flatMap1[F[_]](implicit F: _root_.scalaz.Bind[F]): shims.FlatMap[F] = ???
}

private[shims] trait LowPriorityImplicits1 extends LowPriorityImplicits2 {

  implicit def monad1[F[_]](implicit F: _root_.scalaz.Monad[F]): shims.Monad[F] = ???
}

trait Implicits extends LowPriorityImplicits1 {

  implicit def traverse1[F[_]](implicit F: _root_.scalaz.Traverse[F]): shims.Traverse[F] = ???
}