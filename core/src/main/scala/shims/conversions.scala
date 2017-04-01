package shims

import cats.Eval
import scalaz.\/

import shims.util.{</<, Capture, EitherCapture, OptionCapture}

import java.io.Serializable

sealed trait Synthetic extends Serializable

trait EqConversions {

  private[shims] trait EqShimS2C[A] extends cats.kernel.Eq[A] with Synthetic {
    val A: scalaz.Equal[A]

    override def eqv(x: A, y: A): Boolean = A.equal(x, y)
  }

  implicit def equalToCats[A, T](implicit AC: Capture[scalaz.Equal[A], T], ev: T </< Synthetic): cats.kernel.Eq[A] with Synthetic =
    new EqShimS2C[A] { val A = AC.value }

  private[shims] trait EqShimC2S[A] extends scalaz.Equal[A] with Synthetic {
    val A: cats.kernel.Eq[A]

    override def equal(x: A, y: A): Boolean = A.eqv(x, y)
  }

  implicit def eqToScalaz[A, T](implicit AC: Capture[cats.kernel.Eq[A], T], ev: T </< Synthetic): scalaz.Equal[A] with Synthetic =
    new EqShimC2S[A] { val A = AC.value }
}

trait OrderConversions extends EqConversions {

  private[shims] trait OrderShimS2C[A] extends cats.kernel.Order[A] with EqShimS2C[A] {
    val A: scalaz.Order[A]

    override def compare(x: A, y: A): Int = A.order(x, y).toInt
  }

  implicit def equalToCats[A, T](implicit AC: Capture[scalaz.Order[A], T], ev: T </< Synthetic): cats.kernel.Order[A] with Synthetic =
    new OrderShimS2C[A] { val A = AC.value }

  private[shims] trait OrderShimC2S[A] extends scalaz.Order[A] with EqShimC2S[A] {
    val A: cats.kernel.Order[A]

    override def order(x: A, y: A): scalaz.Ordering = scalaz.Ordering.fromInt(A.compare(x, y))
  }

  implicit def eqToScalaz[A, T](implicit AC: Capture[cats.kernel.Order[A], T], ev: T </< Synthetic): scalaz.Order[A] with Synthetic =
    new OrderShimC2S[A] { val A = AC.value }
}

trait IFunctorConversions {

  private[shims] trait IFunctorShimS2C[F[_]] extends cats.functor.Invariant[F] with Synthetic {
    val F: scalaz.InvariantFunctor[F]

    override def imap[A, B](fa: F[A])(f: A => B)(f2: B => A): F[B] = F.xmap(fa, f, f2)
  }

  implicit def ifunctorToCats[F[_], T](implicit FC: Capture[scalaz.InvariantFunctor[F], T], ev: T </< Synthetic): cats.functor.Invariant[F] with Synthetic =
    new IFunctorShimS2C[F] { val F = FC.value }

  private[shims] trait IFunctorShimC2S[F[_]] extends scalaz.InvariantFunctor[F] with Synthetic {
    val F: cats.functor.Invariant[F]

    override def xmap[A, B](fa: F[A], f: A => B, f2: B => A): F[B] = F.imap(fa)(f)(f2)
  }

  implicit def ifunctorToScalaz[F[_], T](implicit FC: Capture[cats.functor.Invariant[F], T], ev: T </< Synthetic): scalaz.InvariantFunctor[F] with Synthetic =
    new IFunctorShimC2S[F] { val F = FC.value }
}

trait ContravariantConversions extends IFunctorConversions {

  private[shims] trait ContravariantShimS2C[F[_]] extends cats.functor.Contravariant[F] with IFunctorShimS2C[F] {
    val F: scalaz.Contravariant[F]

    override def contramap[A, B](fa: F[A])(f: B => A): F[B] =
      F.contramap(fa)(f)
  }

  implicit def functorToCats[F[_], T](implicit FC: Capture[scalaz.Contravariant[F], T], ev: T </< Synthetic): cats.functor.Contravariant[F] with Synthetic =
    new ContravariantShimS2C[F] { val F = FC.value }

  private[shims] trait ContravariantShimC2S[F[_]] extends scalaz.Contravariant[F] with IFunctorShimC2S[F] {
    val F: cats.functor.Contravariant[F]

    override def contramap[A, B](fa: F[A])(f: B => A): F[B] =
      F.contramap(fa)(f)
  }

  implicit def functorToScalaz[F[_], T](implicit FC: Capture[cats.functor.Contravariant[F], T], ev: T </< Synthetic): scalaz.Contravariant[F] with Synthetic =
    new ContravariantShimC2S[F] { val F = FC.value }
}

trait FunctorConversions extends IFunctorConversions {

  private[shims] trait FunctorShimS2C[F[_]] extends cats.Functor[F] with IFunctorShimS2C[F] {
    val F: scalaz.Functor[F]

    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functorToCats[F[_], T](implicit FC: Capture[scalaz.Functor[F], T], ev: T </< Synthetic): cats.Functor[F] with Synthetic =
    new FunctorShimS2C[F] { val F = FC.value }

  private[shims] trait FunctorShimC2S[F[_]] extends scalaz.Functor[F] with IFunctorShimC2S[F] {
    val F: cats.Functor[F]

    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functorToScalaz[F[_], T](implicit FC: Capture[cats.Functor[F], T], ev: T </< Synthetic): scalaz.Functor[F] with Synthetic =
    new FunctorShimC2S[F] { val F = FC.value }
}

trait ApplyConversions extends FunctorConversions {

  private[shims] trait ApplyShimS2C[F[_]] extends cats.Apply[F] with FunctorShimS2C[F] {
    val F: scalaz.Apply[F]

    override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] =
      F.ap(fa)(ff)
  }

  implicit def applyToCats[F[_], T](implicit FC: Capture[scalaz.Apply[F], T], ev: T </< Synthetic): cats.Apply[F] with Synthetic =
    new ApplyShimS2C[F] { val F = FC.value }

  private[shims] trait ApplyShimC2S[F[_]] extends scalaz.Apply[F] with FunctorShimC2S[F] {
    val F: cats.Apply[F]

    override def ap[A, B](fa: => F[A])(ff: => F[A => B]): F[B] =
      F.ap(ff)(fa)
  }

  implicit def applyToScalaz[F[_], T](implicit FC: Capture[cats.Apply[F], T], ev: T </< Synthetic): scalaz.Apply[F] with Synthetic =
    new ApplyShimC2S[F] { val F = FC.value }
}

trait ApplicativeConversions extends ApplyConversions {

  private[shims] trait ApplicativeShimS2C[F[_]] extends cats.Applicative[F] with ApplyShimS2C[F] {
    val F: scalaz.Applicative[F]

    override def pure[A](x: A): F[A] = F.point(x)
  }

  implicit def applicativeToCats[F[_], T](implicit FC: Capture[scalaz.Applicative[F], T], ev: T </< Synthetic): cats.Applicative[F] with Synthetic =
    new ApplicativeShimS2C[F] { val F = FC.value }

  private[shims] trait ApplicativeShimC2S[F[_]] extends scalaz.Applicative[F] with ApplyShimC2S[F] {
    val F: cats.Applicative[F]

    override def point[A](x: => A): F[A] = F.pure(x)
  }

  implicit def applicativeToScalaz[F[_], T](implicit FC: Capture[cats.Applicative[F], T], ev: T </< Synthetic): scalaz.Applicative[F] with Synthetic =
    new ApplicativeShimC2S[F] { val F = FC.value }
}

trait SemigroupConversions {

  private[shims] trait SemigroupShimS2C[A] extends cats.Semigroup[A] with Synthetic {
    val A: scalaz.Semigroup[A]

    override def combine(x: A, y: A): A = A.append(x, y)
  }

  implicit def semigroupToCats[A, T](implicit FC: Capture[scalaz.Semigroup[A], T], ev: T </< Synthetic): cats.Semigroup[A] with Synthetic =
    new SemigroupShimS2C[A] { val A = FC.value }

  private[shims] trait SemigroupShimC2S[A] extends scalaz.Semigroup[A] with Synthetic {
    val A: cats.Semigroup[A]

    override def append(f1: A, f2: => A): A = A.combine(f1, f2)
  }

  implicit def semigroupToScalaz[A, T](implicit FC: Capture[cats.Semigroup[A], T], ev: T </< Synthetic): scalaz.Semigroup[A] with Synthetic =
    new SemigroupShimC2S[A] { val A = FC.value }
}

trait MonoidConversions extends SemigroupConversions {

  private[shims] trait MonoidShimS2C[A] extends cats.Monoid[A] with SemigroupShimS2C[A] {
    val A: scalaz.Monoid[A]

    override def empty: A = A.zero
  }

  implicit def monoidToCats[A, T](implicit FC: Capture[scalaz.Monoid[A], T], ev: T </< Synthetic): cats.Monoid[A] with Synthetic =
    new MonoidShimS2C[A] { val A = FC.value }

  private[shims] trait MonoidShimC2S[A] extends scalaz.Monoid[A] with SemigroupShimC2S[A] {
    val A: cats.Monoid[A]

    override def zero: A = A.empty
  }

  implicit def monoidToScalaz[A, T](implicit FC: Capture[cats.Monoid[A], T], ev: T </< Synthetic): scalaz.Monoid[A] with Synthetic =
    new MonoidShimC2S[A] { val A = FC.value }
}

trait FoldableConversions extends MonoidConversions {

  private[shims] trait FoldableShimS2C[F[_]] extends cats.Foldable[F] with Synthetic {
    val F: scalaz.Foldable[F]

    override def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
      F.foldLeft(fa, b)(f)

    override def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      F.foldRight(fa, lb)(f(_, _))
  }

  implicit def foldableToCats[F[_], T](implicit FC: Capture[scalaz.Foldable[F], T], ev: T </< Synthetic): cats.Foldable[F] with Synthetic =
    new FoldableShimS2C[F] { val F = FC.value }

  private[shims] trait FoldableShimC2S[F[_]] extends scalaz.Foldable[F] with Synthetic {
    val F: cats.Foldable[F]

    override def foldMap[A, B](fa: F[A])(f: A => B)(implicit B: scalaz.Monoid[B]): B =
      F.foldMap(fa)(f)

    override def foldRight[A, B](fa: F[A], z: => B)(f: (A, => B) => B): B =
      F.foldRight(fa, Eval.always(z))((a, b) => b.map(f(a, _))).value
  }

  implicit def foldableToScalaz[F[_], T](implicit FC: Capture[cats.Foldable[F], T], ev: T </< Synthetic): scalaz.Foldable[F] with Synthetic =
    new FoldableShimC2S[F] { val F = FC.value }
}

trait TraverseConversions extends ApplicativeConversions with FoldableConversions {

  private[shims] trait TraverseShimS2C[F[_]] extends cats.Traverse[F] with FunctorShimS2C[F] with FoldableShimS2C[F] {
    val F: scalaz.Traverse[F]

    override def traverse[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: cats.Applicative[G]): G[F[B]] =
      F.traverse(fa)(f)
  }

  implicit def traverseToCats[F[_], T](implicit FC: Capture[scalaz.Traverse[F], T], ev: T </< Synthetic): cats.Traverse[F] with Synthetic =
    new TraverseShimS2C[F] { val F = FC.value }

  private[shims] trait TraverseShimC2S[F[_]] extends scalaz.Traverse[F] with FunctorShimC2S[F] with FoldableShimC2S[F] {
    val F: cats.Traverse[F]

    override def traverseImpl[G[_], A, B](fa: F[A])(f: A => G[B])(implicit G: scalaz.Applicative[G]): G[F[B]] =
      F.traverse(fa)(f)
  }

  implicit def traverseToScalaz[F[_], T](implicit FC: Capture[cats.Traverse[F], T], ev: T </< Synthetic): scalaz.Traverse[F] with Synthetic =
    new TraverseShimC2S[F] { val F = FC.value }
}

trait CoflatMapConversions extends ApplicativeConversions {

  private[shims] trait CoflatMapShimS2C[F[_]] extends cats.CoflatMap[F] with FunctorShimS2C[F] {
    val F: scalaz.Cobind[F]

    override def coflatMap[A, B](fa: F[A])(f: F[A] => B): F[B] = F.cobind(fa)(f)
  }

  implicit def traverseToCats[F[_], T](implicit FC: Capture[scalaz.Cobind[F], T], ev: T </< Synthetic): cats.CoflatMap[F] with Synthetic =
    new CoflatMapShimS2C[F] { val F = FC.value }

  private[shims] trait CoflatMapShimC2S[F[_]] extends scalaz.Cobind[F] with FunctorShimC2S[F] {
    val F: cats.CoflatMap[F]

    override def cobind[A, B](fa: F[A])(f: F[A] => B): F[B] = F.coflatMap(fa)(f)
  }

  implicit def traverseToScalaz[F[_], T](implicit FC: Capture[cats.CoflatMap[F], T], ev: T </< Synthetic): scalaz.Cobind[F] with Synthetic =
    new CoflatMapShimC2S[F] { val F = FC.value }
}

trait ComonadConversions extends CoflatMapConversions {

  private[shims] trait ComonadShimS2C[F[_]] extends cats.Comonad[F] with CoflatMapShimS2C[F] {
    val F: scalaz.Comonad[F]

    override def extract[A](x: F[A]): A = F.copoint(x)
  }

  implicit def traverseToCats[F[_], T](implicit FC: Capture[scalaz.Comonad[F], T], ev: T </< Synthetic): cats.Comonad[F] with Synthetic =
    new ComonadShimS2C[F] { val F = FC.value }

  private[shims] trait ComonadShimC2S[F[_]] extends scalaz.Comonad[F] with CoflatMapShimC2S[F] {
    val F: cats.Comonad[F]

    override def copoint[A](x: F[A]): A = F.extract(x)
  }

  implicit def traverseToScalaz[F[_], T](implicit FC: Capture[cats.Comonad[F], T], ev: T </< Synthetic): scalaz.Comonad[F] with Synthetic =
    new ComonadShimC2S[F] { val F = FC.value }
}

trait FlatMapConversions extends ApplyConversions {

  private[shims] trait FlatMapShimS2C[F[_]] extends cats.FlatMap[F] with ApplyShimS2C[F] {
    val F: scalaz.Bind[F]

    def AppOrBindRec: Either[scalaz.Applicative[F], scalaz.BindRec[F]]

    override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.bind(fa)(f)

    override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = {
      val unsafe = AppOrBindRec.left.map(unsafeTailRecM(_)(a)(f))
      val delegate = unsafe.right.map(_.tailrecM((a: A) => F.map(f(a))(_.asScalaz))(a))

      delegate.merge
    }

    // will not be stack-safe unless F is stack-safe
    private def unsafeTailRecM[A, B](FA: scalaz.Applicative[F])(a: A)(f: A => F[Either[A, B]]): F[B] =
      F.join(F.map(f(a))(e => e.fold(a => unsafeTailRecM(FA)(a)(f), b => FA.point(b))))
  }

  implicit def bindToCats[F[_], T1, T2](
    implicit
      FC: Capture[scalaz.Bind[F], T1],
      EFC: EitherCapture[scalaz.Applicative[F], scalaz.BindRec[F], T2],
      ev1: T1 </< Synthetic,
      ev2: T2 </< Synthetic): cats.FlatMap[F] with Synthetic =
    new FlatMapShimS2C[F] { val F = FC.value; val AppOrBindRec = EFC.value }

  private[shims] trait BindRecShimC2S[F[_]] extends scalaz.BindRec[F] with ApplyShimC2S[F] {
    val F: cats.FlatMap[F]

    override def bind[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)

    override def tailrecM[A, B](f: A => F[A \/ B])(a: A): F[B] =
      F.tailRecM(a)((a: A) => F.map(f(a))(_.asCats))
  }

  implicit def flatMapToScalaz[F[_], T](implicit FC: Capture[cats.FlatMap[F], T], ev: T </< Synthetic): scalaz.BindRec[F] with Synthetic =
    new BindRecShimC2S[F] { val F = FC.value }
}

trait MonadConversions extends ApplicativeConversions with FlatMapConversions {

  private[shims] trait MonadShimS2C[F[_]] extends cats.Monad[F] with ApplicativeShimS2C[F] with FlatMapShimS2C[F] {
    val F: scalaz.Monad[F]

    def OptBindRec: Option[scalaz.BindRec[F]]
    final def AppOrBindRec = OptBindRec.map(Right(_)).getOrElse(Left(F))
  }

  implicit def monadToCats[F[_], T1, T2](
    implicit
      FC: Capture[scalaz.Monad[F], T1],
      OFC: OptionCapture[scalaz.BindRec[F], T2],
      ev1: T1 </< Synthetic,
      ev2: T2 </< Synthetic): cats.Monad[F] with Synthetic =
    new MonadShimS2C[F] { val F = FC.value; val OptBindRec = OFC.value }

  private[shims] trait MonadShimC2S[F[_]] extends scalaz.Monad[F] with ApplicativeShimC2S[F] with BindRecShimC2S[F] {
    val F: cats.Monad[F]
  }

  implicit def monadToScalaz[F[_], T](implicit FC: Capture[cats.Monad[F], T], ev: T </< Synthetic): scalaz.Monad[F] with Synthetic =
    new MonadShimC2S[F] { val F = FC.value }
}

trait BifunctorConversions {

  private[shims] trait BifunctorShimS2C[F[_, _]] extends cats.functor.Bifunctor[F] with Synthetic {
    val F: scalaz.Bifunctor[F]

    override def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D] =
      F.bimap(fab)(f, g)
  }

  implicit def ifunctorToCats[F[_, _], T](implicit FC: Capture[scalaz.Bifunctor[F], T], ev: T </< Synthetic): cats.functor.Bifunctor[F] with Synthetic =
    new BifunctorShimS2C[F] { val F = FC.value }

  private[shims] trait BifunctorShimC2S[F[_, _]] extends scalaz.Bifunctor[F] with Synthetic {
    val F: cats.functor.Bifunctor[F]

    override def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D] =
      F.bimap(fab)(f, g)
  }

  implicit def ifunctorToScalaz[F[_, _], T](implicit FC: Capture[cats.functor.Bifunctor[F], T], ev: T </< Synthetic): scalaz.Bifunctor[F] with Synthetic =
    new BifunctorShimC2S[F] { val F = FC.value }
}

trait BifoldableConversions extends MonoidConversions {

  private[shims] trait BifoldableShimS2C[F[_, _]] extends cats.Bifoldable[F] with Synthetic {
    val F: scalaz.Bifoldable[F]

    override def bifoldLeft[A, B, C](fab: F[A, B], c: C)(f: (C, A) => C, g: (C, B) => C): C =
      F.bifoldLeft(fab, c)(f)(g)

    override def bifoldRight[A, B, C](fab: F[A, B], c: Eval[C])(f: (A, Eval[C]) => Eval[C], g: (B, Eval[C]) => Eval[C]): Eval[C] =
      F.bifoldRight(fab, c)((a, c) => f(a, c))((b, c) => g(b, c))
  }

  implicit def ifunctorToCats[F[_, _], T](implicit FC: Capture[scalaz.Bifoldable[F], T], ev: T </< Synthetic): cats.Bifoldable[F] with Synthetic =
    new BifoldableShimS2C[F] { val F = FC.value }

  private[shims] trait BifoldableShimC2S[F[_, _]] extends scalaz.Bifoldable[F] with Synthetic {
    val F: cats.Bifoldable[F]

    override def bifoldMap[A, B, M: scalaz.Monoid](fa: F[A, B])(f: A => M)(g: B => M): M =
      F.bifoldMap(fa)(f, g)

    override def bifoldRight[A, B, C](fa: F[A, B], z: => C)(f: (A, => C) => C)(g: (B, => C) => C): C =
      F.bifoldRight(fa, Eval.always(z))((a, c) => c.map(f(a, _)), (b, c) => c.map(g(b, _))).value
  }

  implicit def ifunctorToScalaz[F[_, _], T](implicit FC: Capture[cats.Bifoldable[F], T], ev: T </< Synthetic): scalaz.Bifoldable[F] with Synthetic =
    new BifoldableShimC2S[F] { val F = FC.value }
}

trait BitraverseConversions extends BifunctorConversions with BifoldableConversions with ApplicativeConversions {

  private[shims] trait BitraverseShimS2C[F[_, _]] extends cats.Bitraverse[F] with BifunctorShimS2C[F] with BifoldableShimS2C[F] {
    val F: scalaz.Bitraverse[F]

    override def bitraverse[G[_]: cats.Applicative, A, B, C, D](fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]] =
      F.bitraverse(fab)(f)(g)
  }

  implicit def ifunctorToCats[F[_, _], T](implicit FC: Capture[scalaz.Bitraverse[F], T], ev: T </< Synthetic): cats.Bitraverse[F] with Synthetic =
    new BitraverseShimS2C[F] { val F = FC.value }

  private[shims] trait BitraverseShimC2S[F[_, _]] extends scalaz.Bitraverse[F] with BifunctorShimC2S[F] with BifoldableShimC2S[F] {
    val F: cats.Bitraverse[F]

    override def bitraverseImpl[G[_]: scalaz.Applicative, A, B, C, D](fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]] =
      F.bitraverse(fab)(f, g)
  }

  implicit def ifunctorToScalaz[F[_, _], T](implicit FC: Capture[cats.Bitraverse[F], T], ev: T </< Synthetic): scalaz.Bitraverse[F] with Synthetic =
    new BitraverseShimC2S[F] { val F = FC.value }
}
