/*
 * Copyright 2018 Daniel Spiewak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shims.conversions

import cats.Eval
import scalaz.\/

import shims.AsSyntax
import shims.util.{Capture, EitherCapture, OptionCapture}

trait IFunctorConversions {

  private[conversions] trait IFunctorShimS2C[F[_]] extends cats.Invariant[F] with Synthetic {
    val F: scalaz.InvariantFunctor[F]

    override def imap[A, B](fa: F[A])(f: A => B)(f2: B => A): F[B] = F.xmap(fa, f, f2)
  }

  implicit def ifunctorToCats[F[_]](implicit FC: Capture[scalaz.InvariantFunctor[F]]): cats.Invariant[F] with Synthetic =
    new IFunctorShimS2C[F] { val F = FC.value }

  private[conversions] trait IFunctorShimC2S[F[_]] extends scalaz.InvariantFunctor[F] with Synthetic {
    val F: cats.Invariant[F]

    override def xmap[A, B](fa: F[A], f: A => B, f2: B => A): F[B] = F.imap(fa)(f)(f2)
  }

  implicit def ifunctorToScalaz[F[_]](implicit FC: Capture[cats.Invariant[F]]): scalaz.InvariantFunctor[F] with Synthetic =
    new IFunctorShimC2S[F] { val F = FC.value }
}

trait ContravariantConversions extends IFunctorConversions {

  private[conversions] trait ContravariantShimS2C[F[_]] extends cats.Contravariant[F] with IFunctorShimS2C[F] {
    val F: scalaz.Contravariant[F]

    override def contramap[A, B](fa: F[A])(f: B => A): F[B] =
      F.contramap(fa)(f)
  }

  implicit def contravariantToCats[F[_]](implicit FC: Capture[scalaz.Contravariant[F]]): cats.Contravariant[F] with Synthetic =
    new ContravariantShimS2C[F] { val F = FC.value }

  private[conversions] trait ContravariantShimC2S[F[_]] extends scalaz.Contravariant[F] with IFunctorShimC2S[F] {
    val F: cats.Contravariant[F]

    override def contramap[A, B](fa: F[A])(f: B => A): F[B] =
      F.contramap(fa)(f)
  }

  implicit def contravariantToScalaz[F[_]](implicit FC: Capture[cats.Contravariant[F]]): scalaz.Contravariant[F] with Synthetic =
    new ContravariantShimC2S[F] { val F = FC.value }
}

trait FunctorConversions extends IFunctorConversions with ContravariantConversions {

  private[conversions] trait FunctorShimS2C[F[_]] extends cats.Functor[F] with IFunctorShimS2C[F] {
    val F: scalaz.Functor[F]

    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functorToCats[F[_]](implicit FC: Capture[scalaz.Functor[F]]): cats.Functor[F] with Synthetic =
    new FunctorShimS2C[F] { val F = FC.value }

  private[conversions] trait FunctorShimC2S[F[_]] extends scalaz.Functor[F] with IFunctorShimC2S[F] {
    val F: cats.Functor[F]

    override def map[A, B](fa: F[A])(f: A => B): F[B] = F.map(fa)(f)
  }

  implicit def functorToScalaz[F[_]](implicit FC: Capture[cats.Functor[F]]): scalaz.Functor[F] with Synthetic =
    new FunctorShimC2S[F] { val F = FC.value }
}

trait ApplyConversions extends FunctorConversions {

  private[conversions] trait ApplyShimS2C[F[_]] extends cats.Apply[F] with FunctorShimS2C[F] {
    val F: scalaz.Apply[F]

    override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] =
      F.ap(fa)(ff)
  }

  implicit def applyToCats[F[_]](implicit FC: Capture[scalaz.Apply[F]]): cats.Apply[F] with Synthetic =
    new ApplyShimS2C[F] { val F = FC.value }

  private[conversions] trait ApplyShimC2S[F[_]] extends scalaz.Apply[F] with FunctorShimC2S[F] {
    val F: cats.Apply[F]

    override def ap[A, B](fa: => F[A])(ff: => F[A => B]): F[B] =
      F.ap(ff)(fa)
  }

  implicit def applyToScalaz[F[_]](implicit FC: Capture[cats.Apply[F]]): scalaz.Apply[F] with Synthetic =
    new ApplyShimC2S[F] { val F = FC.value }
}

trait ApplicativeConversions extends ApplyConversions {

  private[conversions] trait ApplicativeShimS2C[F[_]] extends cats.Applicative[F] with ApplyShimS2C[F] {
    val F: scalaz.Applicative[F]

    override def pure[A](x: A): F[A] = F.point(x)
  }

  implicit def applicativeToCats[F[_]](implicit FC: Capture[scalaz.Applicative[F]]): cats.Applicative[F] with Synthetic =
    new ApplicativeShimS2C[F] { val F = FC.value }

  private[conversions] trait ApplicativeShimC2S[F[_]] extends scalaz.Applicative[F] with ApplyShimC2S[F] {
    val F: cats.Applicative[F]

    override def point[A](x: => A): F[A] = F.pure(x)
  }

  implicit def applicativeToScalaz[F[_]](implicit FC: Capture[cats.Applicative[F]]): scalaz.Applicative[F] with Synthetic =
    new ApplicativeShimC2S[F] { val F = FC.value }
}

trait AlternativeConversions extends ApplicativeConversions with MonoidKConversions {

  private[conversions] trait AlternativeShimS2C[F[_]] extends cats.Alternative[F] with ApplicativeShimS2C[F] with MonoidKShimS2C[F] {
    val F: scalaz.ApplicativePlus[F]
  }

  implicit def applicativePlusToCats[F[_]](implicit FC: Capture[scalaz.ApplicativePlus[F]]): cats.Alternative[F] with Synthetic =
    new AlternativeShimS2C[F] { val F = FC.value }

  private[conversions] trait ApplicativePlusShimC2S[F[_]] extends scalaz.ApplicativePlus[F] with ApplicativeShimC2S[F] with PlusEmptyShimC2S[F] {
    val F: cats.Alternative[F]
  }

  implicit def alternativeToScalaz[F[_]](implicit FC: Capture[cats.Alternative[F]]): scalaz.ApplicativePlus[F] with Synthetic =
    new ApplicativePlusShimC2S[F] { val F = FC.value }
}

trait FoldableConversions extends MonoidConversions with ApplicativeConversions {

  private[conversions] trait FoldableShimS2C[F[_]] extends cats.Foldable[F] with Synthetic {
    val F: scalaz.Foldable[F]

    override def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
      F.foldLeft(fa, b)(f)

    override def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      F.foldRight(fa, lb)(f(_, _))
  }

  implicit def foldableToCats[F[_]](implicit FC: Capture[scalaz.Foldable[F]]): cats.Foldable[F] with Synthetic =
    new FoldableShimS2C[F] { val F = FC.value }

  private[conversions] trait FoldableShimC2S[F[_]] extends scalaz.Foldable[F] with Synthetic {
    val F: cats.Foldable[F]

    /*
     * There are a couple of these "explicitly use the conversion that should
     * be implicit" bits.  I don't have a good explanation for it, but I can say
     * that commenting it out, compiling the whole project, and then uncommenting
     * it WORKS, while clean compiling does not.  That leads me to suspect that
     * it's some sort of scope linearization issue inside of nsc.  I can't really
     * fix that, and it's not likely to be something that would affect client
     * code (since shims is already compiled by that point), so I'm just going to
     * be explicit.
     */
    override def foldMap[A, B](fa: F[A])(f: A => B)(implicit B: scalaz.Monoid[B]): B =
      F.foldMap(fa)(f)(monoidToCats(Capture(B)))

    override def foldRight[A, B](fa: F[A], z: => B)(f: (A, => B) => B): B =
      F.foldRight(fa, Eval.always(z))((a, b) => b.map(f(a, _))).value
  }

  implicit def foldableToScalaz[F[_]](implicit FC: Capture[cats.Foldable[F]]): scalaz.Foldable[F] with Synthetic =
    new FoldableShimC2S[F] { val F = FC.value }
}

trait TraverseConversions extends ApplicativeConversions with FoldableConversions {

  private[conversions] trait TraverseShimS2C[F[_]] extends cats.Traverse[F] with FunctorShimS2C[F] with FoldableShimS2C[F] {
    val F: scalaz.Traverse[F]

    override def traverse[G[_]: cats.Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] =
      F.traverse(fa)(f)(applicativeToScalaz(Capture(cats.Applicative[G])))
  }

  implicit def traverseToCats[F[_]](implicit FC: Capture[scalaz.Traverse[F]]): cats.Traverse[F] with Synthetic =
    new TraverseShimS2C[F] { val F = FC.value }

  private[conversions] trait TraverseShimC2S[F[_]] extends scalaz.Traverse[F] with FunctorShimC2S[F] with FoldableShimC2S[F] {
    val F: cats.Traverse[F]

    override def traverseImpl[G[_]: scalaz.Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] =
      F.traverse(fa)(f)(applicativeToCats(Capture(scalaz.Applicative[G])))
  }

  implicit def traverseToScalaz[F[_]](implicit FC: Capture[cats.Traverse[F]]): scalaz.Traverse[F] with Synthetic =
    new TraverseShimC2S[F] { val F = FC.value }
}

trait CoflatMapConversions extends ApplicativeConversions with TraverseConversions {

  private[conversions] trait CoflatMapShimS2C[F[_]] extends cats.CoflatMap[F] with FunctorShimS2C[F] {
    val F: scalaz.Cobind[F]

    override def coflatMap[A, B](fa: F[A])(f: F[A] => B): F[B] = F.cobind(fa)(f)
  }

  implicit def cobindToCats[F[_]](implicit FC: Capture[scalaz.Cobind[F]]): cats.CoflatMap[F] with Synthetic =
    new CoflatMapShimS2C[F] { val F = FC.value }

  private[conversions] trait CoflatMapShimC2S[F[_]] extends scalaz.Cobind[F] with FunctorShimC2S[F] {
    val F: cats.CoflatMap[F]

    override def cobind[A, B](fa: F[A])(f: F[A] => B): F[B] = F.coflatMap(fa)(f)
  }

  implicit def coflatMapToScalaz[F[_]](implicit FC: Capture[cats.CoflatMap[F]]): scalaz.Cobind[F] with Synthetic =
    new CoflatMapShimC2S[F] { val F = FC.value }
}

trait ComonadConversions extends CoflatMapConversions {

  private[conversions] trait ComonadShimS2C[F[_]] extends cats.Comonad[F] with CoflatMapShimS2C[F] {
    val F: scalaz.Comonad[F]

    override def extract[A](x: F[A]): A = F.copoint(x)
  }

  implicit def comonadToCats[F[_]](implicit FC: Capture[scalaz.Comonad[F]]): cats.Comonad[F] with Synthetic =
    new ComonadShimS2C[F] { val F = FC.value }

  private[conversions] trait ComonadShimC2S[F[_]] extends scalaz.Comonad[F] with CoflatMapShimC2S[F] {
    val F: cats.Comonad[F]

    override def copoint[A](x: F[A]): A = F.extract(x)
  }

  implicit def comonadToScalaz[F[_]](implicit FC: Capture[cats.Comonad[F]]): scalaz.Comonad[F] with Synthetic =
    new ComonadShimC2S[F] { val F = FC.value }
}

trait FlatMapConversions extends ApplyConversions with ComonadConversions {

  private[conversions] trait FlatMapShimS2C[F[_]] extends cats.FlatMap[F] with ApplyShimS2C[F] {
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

  implicit def bindToCats[F[_]](
    implicit
      FC: Capture[scalaz.Bind[F]],
      EFC: EitherCapture[scalaz.Applicative[F], scalaz.BindRec[F]]): cats.FlatMap[F] with Synthetic =
    new FlatMapShimS2C[F] { val F = FC.value; val AppOrBindRec = EFC.value }

  private[conversions] trait BindRecShimC2S[F[_]] extends scalaz.BindRec[F] with ApplyShimC2S[F] {
    val F: cats.FlatMap[F]

    override def bind[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)

    override def tailrecM[A, B](f: A => F[A \/ B])(a: A): F[B] =
      F.tailRecM(a)((a: A) => F.map(f(a))(_.asCats))
  }

  implicit def flatMapToScalaz[F[_]](implicit FC: Capture[cats.FlatMap[F]]): scalaz.BindRec[F] with Synthetic =
    new BindRecShimC2S[F] { val F = FC.value }
}

trait MonadConversions extends ApplicativeConversions with FlatMapConversions {

  private[conversions] trait MonadShimS2C[F[_]] extends cats.Monad[F] with ApplicativeShimS2C[F] with FlatMapShimS2C[F] {
    val F: scalaz.Monad[F]

    def OptBindRec: Option[scalaz.BindRec[F]]
    final def AppOrBindRec = OptBindRec.map(Right(_)).getOrElse(Left(F))
  }

  implicit def monadToCats[F[_]](
    implicit
      FC: Capture[scalaz.Monad[F]],
      OFC: OptionCapture[scalaz.BindRec[F]]): cats.Monad[F] with Synthetic =
    new MonadShimS2C[F] { val F = FC.value; val OptBindRec = OFC.value }

  private[conversions] trait MonadShimC2S[F[_]] extends scalaz.Monad[F] with ApplicativeShimC2S[F] with BindRecShimC2S[F] {
    val F: cats.Monad[F]
  }

  implicit def monadToScalaz[F[_]](implicit FC: Capture[cats.Monad[F]]): scalaz.Monad[F] with Synthetic =
    new MonadShimC2S[F] { val F = FC.value }
}

trait MonadErrorConversions extends MonadConversions {

  private[conversions] trait MonadErrorShimS2C[F[_], E] extends cats.MonadError[F, E] with MonadShimS2C[F] {
    val F: scalaz.MonadError[F, E]

    override def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A] = F.handleError(fa)(f)

    override def raiseError[A](e: E): F[A] = F.raiseError(e)
  }

  implicit def monadErrorToCats[F[_], E](
    implicit
      FC: Capture[scalaz.MonadError[F, E]],
      OFC: OptionCapture[scalaz.BindRec[F]]): cats.MonadError[F, E] with Synthetic =
    new MonadErrorShimS2C[F, E] { val F = FC.value; val OptBindRec = OFC.value }

  private[conversions] trait MonadErrorShimC2S[F[_], E] extends scalaz.MonadError[F, E] with MonadShimC2S[F] {
    val F: cats.MonadError[F, E]

    override def handleError[A](fa: F[A])(f: E => F[A]): F[A] = F.handleErrorWith(fa)(f)
    override def raiseError[A](e: E): F[A] = F.raiseError(e)
  }

  implicit def monadErrorToScalaz[F[_], E](implicit FC: Capture[cats.MonadError[F, E]]): scalaz.MonadError[F, E] with Synthetic =
    new MonadErrorShimC2S[F, E] { val F = FC.value }
}
