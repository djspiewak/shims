package shims

import shims.util.{</<, Capture}

sealed trait Synthetic

trait EqConversions {

  implicit def equalToCats[A, T](implicit A: Capture[scalaz.Equal[A], T], ev: T </< Synthetic): cats.Eq[A] with Synthetic =
    new cats.Eq[A] with Synthetic {
      def eqv(a1: A, a2: A) = A.value.equal(a1, a2)
    }

  implicit def eqToScalaz[A, T](implicit A: Capture[cats.Eq[A], T], ev: T </< Synthetic): scalaz.Equal[A] with Synthetic =
    new scalaz.Equal[A] with Synthetic {
      def equal(a1: A, a2: A) = A.value.eqv(a1, a2)
    }
}

trait IFunctorConversions {

  implicit def ifunctorToCats[F[_], T](implicit F: Capture[scalaz.InvariantFunctor[F], T], ev: T </< Synthetic): cats.functor.Invariant[F] with Synthetic =
    new cats.functor.Invariant[F] with Synthetic {
      def imap[A, B](fa: F[A])(f: A => B)(f2: B => A): F[B] = F.value.xmap(fa, f, f2)
    }

  implicit def ifunctorToScalaz[F[_], T](implicit F: Capture[cats.functor.Invariant[F], T], ev: T </< Synthetic): scalaz.InvariantFunctor[F] with Synthetic =
    new scalaz.InvariantFunctor[F] with Synthetic {
      def xmap[A, B](fa: F[A], f: A => B, f2: B => A): F[B] = F.value.imap(fa)(f)(f2)
    }
}

trait FunctorConversions extends IFunctorConversions {

  implicit def functorToCats[F[_], T](implicit F: Capture[scalaz.Functor[F], T], ev: T </< Synthetic): cats.Functor[F] with Synthetic =
    new cats.Functor[F] with Synthetic {
      def map[A, B](fa: F[A])(f: A => B): F[B] = F.value.map(fa)(f)
    }

  implicit def functorToScalaz[F[_], T](implicit F: Capture[cats.Functor[F], T], ev: T </< Synthetic): scalaz.Functor[F] with Synthetic =
    new scalaz.Functor[F] with Synthetic {
      def map[A, B](fa: F[A])(f: A => B): F[B] = F.value.map(fa)(f)
    }
}
