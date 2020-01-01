/*
 * Copyright 2020 Daniel Spiewak
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

// topological root(s): EitherConverters, FunctionKConverters, EvalConverters, StateTConverters, NELConverters
package shims.conversions

import shims.util.{Capture, OptionCapture}

import scalaz.{~>, \/, \&/}

import cats.arrow.FunctionK
import cats.instances.either._
import cats.syntax.bifunctor._

trait AsScalaz[-I, +O] {
  def c2s(i: I): O
}

trait AsCats[-I, +O] {
  def s2c(i: I): O
}

trait EitherConverters {

  implicit def eitherAs[A, B] = new AsScalaz[Either[A, B], A \/ B] with AsCats[A \/ B, Either[A, B]] {
    def c2s(e: Either[A, B]) = \/.fromEither(e)
    def s2c(e: A \/ B) = e.fold(l => Left(l), r => Right(r))
  }
}

trait FunctionKConverters {

  implicit def functionkAs[F[_], G[_]] = new AsScalaz[FunctionK[F, G], F ~> G] with AsCats[F ~> G, FunctionK[F, G]] {
    def c2s(f: FunctionK[F, G]) = λ[F ~> G](f(_))
    def s2c(f: F ~> G) = λ[FunctionK[F, G]](f(_))
  }
}

trait FreeConverters extends MonadConversions {

  implicit def freeAs[S[_], A] = new AsScalaz[cats.free.Free[S, A], scalaz.Free[S, A]] with AsCats[scalaz.Free[S, A], cats.free.Free[S, A]] {

    def c2s(f: cats.free.Free[S, A]) =
      f.foldMap(λ[FunctionK[S, scalaz.Free[S, ?]]](scalaz.Free.liftF(_)))(
        monadToCats(
          Capture(scalaz.Monad[scalaz.Free[S, ?]]),
          OptionCapture(None)))

    def s2c(f: scalaz.Free[S, A]) =
      f.foldMap[cats.free.Free[S, ?]](λ[S ~> cats.free.Free[S, ?]](cats.free.Free.liftF(_)))(
        monadToScalaz(Capture(cats.Monad[cats.free.Free[S, ?]])))
  }
}

trait EvalConverters extends FreeConverters {
  import cats.Eval
  import scalaz.{Name, Need, Trampoline, Value}
  import scalaz.Free.{Trampoline => FT}

  implicit def evalAs[A] = new AsScalaz[Eval[A], FT[A]] with AsCats[FT[A], Eval[A]] {

    // the inner workings of eval aren't exposed, so we can't do any better here
    def c2s(e: Eval[A]) = Trampoline.delay(e.value)

    def s2c(t: FT[A]) =
      t.foldMap(λ[Function0 ~> Eval](a => Eval.always(a())))(
        monadToScalaz(Capture(cats.Monad[Eval])))
  }

  implicit def nameAs[A] = new AsCats[Name[A], Eval[A]] {
    def s2c(i: Name[A]): Eval[A] = Eval.always(i.value)
  }

  implicit def needAs[A] = new AsCats[Need[A], Eval[A]] {
    // this caches twice, but correctly reflects structure; maybe change?
    def s2c(i: Need[A]): Eval[A] = Eval.later(i.value)
  }

  implicit def valueAs[A] = new AsCats[Value[A], Eval[A]] {
    def s2c(i: Value[A]): Eval[A] = Eval.now(i.value)
  }
}

trait IndexedStateTConverters extends MonadConversions {

  implicit def stateTAs[F[_]: cats.Monad, S1, S2, A] =
    new AsScalaz[cats.data.IndexedStateT[F, S1, S2, A], scalaz.IndexedStateT[F, S1, S2, A]] with AsCats[scalaz.IndexedStateT[F, S1, S2, A], cats.data.IndexedStateT[F, S1, S2, A]] {

      def c2s(st: cats.data.IndexedStateT[F, S1, S2, A]) =
        scalaz.IndexedStateT[F, S1, S2, A](s => cats.Monad[F].flatMap(st.runF)(_(s)))(
          monadToScalaz(Capture(cats.Monad[F])))

      def s2c(st: scalaz.IndexedStateT[F, S1, S2, A]) =
        cats.data.IndexedStateT[F, S1, S2, A](st.run(_)(monadToScalaz(Capture(cats.Monad[F]))))
    }
}

trait NELConverters {

  implicit def nelAs[A] = new AsScalaz[cats.data.NonEmptyList[A], scalaz.NonEmptyList[A]] with AsCats[scalaz.NonEmptyList[A], cats.data.NonEmptyList[A]] {

    def c2s(nel: cats.data.NonEmptyList[A]) = scalaz.NonEmptyList(nel.head, nel.tail: _*)
    def s2c(nel: scalaz.NonEmptyList[A]) = cats.data.NonEmptyList(nel.head, nel.tail.toList)
  }
}

trait EitherKConverters {

  implicit def eitherKAs[F[_], G[_], A] = new AsScalaz[cats.data.EitherK[F, G, A], scalaz.Coproduct[F, G, A]] with AsCats[scalaz.Coproduct[F, G, A], cats.data.EitherK[F, G, A]] {

    def s2c(i: scalaz.Coproduct[F, G, A]): cats.data.EitherK[F, G, A] =
      cats.data.EitherK(i.run.toEither)

    def c2s(i: cats.data.EitherK[F, G, A]): scalaz.Coproduct[F, G, A] =
      scalaz.Coproduct(\/.fromEither(i.run))
  }
}

trait EitherTConverters extends EitherConverters {

  implicit def eitherTAs[F[_], A, B](implicit F: cats.Functor[F]) = new AsScalaz[cats.data.EitherT[F, A, B], scalaz.EitherT[F, A, B]] with AsCats[scalaz.EitherT[F, A, B], cats.data.EitherT[F, A, B]] {

    def s2c(i: scalaz.EitherT[F, A, B]): cats.data.EitherT[F, A, B] = cats.data.EitherT(F.map(i.run)(eitherAs[A, B].s2c(_)))
    def c2s(i: cats.data.EitherT[F, A, B]): scalaz.EitherT[F, A, B] = scalaz.EitherT(F.map(i.value)(eitherAs[A, B].c2s(_)))
  }
}

trait KleisliConverters {

  implicit def kleisliAs[F[_], A, B] = new AsScalaz[cats.data.Kleisli[F, A, B], scalaz.Kleisli[F, A, B]] with AsCats[scalaz.Kleisli[F, A, B], cats.data.Kleisli[F, A, B]] {

    def s2c(i: scalaz.Kleisli[F, A, B]): cats.data.Kleisli[F, A, B] = cats.data.Kleisli(i.run)
    def c2s(i: cats.data.Kleisli[F, A, B]): scalaz.Kleisli[F, A, B] = scalaz.Kleisli(i.run)
  }
}

trait OptionTConverters {

  implicit def optionTAs[F[_], A] = new AsScalaz[cats.data.OptionT[F, A], scalaz.OptionT[F, A]] with AsCats[scalaz.OptionT[F, A], cats.data.OptionT[F, A]] {

    def s2c(i: scalaz.OptionT[F, A]): cats.data.OptionT[F, A] = cats.data.OptionT(i.run)
    def c2s(i: cats.data.OptionT[F, A]): scalaz.OptionT[F, A] = scalaz.OptionT(i.value)
  }
}

trait ValidatedConverters {

  implicit def validatedAs[E, A] = new AsScalaz[cats.data.Validated[E, A], scalaz.Validation[E, A]] with AsCats[scalaz.Validation[E, A], cats.data.Validated[E, A]] {

    def s2c(i: scalaz.Validation[E, A]): cats.data.Validated[E, A] =
      cats.data.Validated.fromEither(i.disjunction.toEither)

    def c2s(i: cats.data.Validated[E, A]): scalaz.Validation[E, A] =
      scalaz.Validation.fromEither(i.toEither)
  }
}

trait ValidatedNELConverters extends ValidatedConverters with NELConverters {

  implicit def validatedNelAs[E, A] = new AsScalaz[cats.data.ValidatedNel[E, A], scalaz.ValidationNel[E, A]] with AsCats[scalaz.ValidationNel[E, A], cats.data.ValidatedNel[E, A]] {

    def s2c(i: scalaz.ValidationNel[E, A]): cats.data.ValidatedNel[E, A] =
      cats.data.Validated.fromEither(i.disjunction.toEither.leftMap(nelAs[E].s2c(_)))

    def c2s(i: cats.data.ValidatedNel[E, A]): scalaz.ValidationNel[E, A] =
      scalaz.Validation.fromEither(i.toEither.leftMap(nelAs[E].c2s(_)))
  }
}

trait OneAndConverters {

  implicit def oneAndAs[F[_], A] = new AsScalaz[cats.data.OneAnd[F, A], scalaz.OneAnd[F, A]] with AsCats[scalaz.OneAnd[F, A], cats.data.OneAnd[F, A]] {

    def s2c(i: scalaz.OneAnd[F, A]): cats.data.OneAnd[F, A] = cats.data.OneAnd(i.head, i.tail)
    def c2s(i: cats.data.OneAnd[F, A]): scalaz.OneAnd[F, A] = scalaz.OneAnd(i.head, i.tail)
  }
}

trait MaybeConverters {

  implicit def maybeAs[A] = new AsCats[scalaz.Maybe[A], Option[A]] {
    def s2c(i: scalaz.Maybe[A]): Option[A] = i.toOption
  }
}

trait MaybeTConverters {

  implicit def maybeTAs[F[_], A](implicit F: scalaz.Functor[F]) =
    new AsCats[scalaz.MaybeT[F, A], cats.data.OptionT[F, A]] {
      def s2c(i: scalaz.MaybeT[F, A]): cats.data.OptionT[F, A] =
        cats.data.OptionT(F.map(i.run)(_.toOption))
    }
}

trait WriterTConverters {

  implicit def writerTAs[F[_], W, A] = new AsScalaz[cats.data.WriterT[F, W, A], scalaz.WriterT[F, W, A]] with AsCats[scalaz.WriterT[F, W, A], cats.data.WriterT[F, W, A]] {

    def s2c(i: scalaz.WriterT[F, W, A]): cats.data.WriterT[F, W, A] = cats.data.WriterT(i.run)
    def c2s(i: cats.data.WriterT[F, W, A]): scalaz.WriterT[F, W, A] = scalaz.WriterT(i.run)
  }
}

trait IorConverters {

  implicit def iorAs[A, B] = new AsScalaz[cats.data.Ior[A, B], A \&/ B] with AsCats[A \&/ B, cats.data.Ior[A, B]] {
    import cats.data.Ior

    def s2c(i: A \&/ B): Ior[A, B] = i.fold(Ior.left(_), Ior.right(_), Ior.both(_, _))
    def c2s(i: Ior[A, B]): A \&/ B = i.fold(\&/.This(_), \&/.That(_), \&/(_, _))
  }
}

trait ConstConverters {

  implicit def constAs[A, B] = new AsScalaz[cats.data.Const[A, B], scalaz.Const[A, B]] with AsCats[scalaz.Const[A, B], cats.data.Const[A, B]] {

    def s2c(i: scalaz.Const[A, B]): cats.data.Const[A, B] = cats.data.Const(i.getConst)
    def c2s(i: cats.data.Const[A, B]): scalaz.Const[A, B] = scalaz.Const(i.getConst)
  }
}

trait CokleisliConverters {

  implicit def cokleisliAs[F[_], A, B] = new AsScalaz[cats.data.Cokleisli[F, A, B], scalaz.Cokleisli[F, A, B]] with AsCats[scalaz.Cokleisli[F, A, B], cats.data.Cokleisli[F, A, B]] {

    def s2c(i: scalaz.Cokleisli[F, A, B]): cats.data.Cokleisli[F, A, B] = cats.data.Cokleisli(i.run)
    def c2s(i: cats.data.Cokleisli[F, A, B]): scalaz.Cokleisli[F, A, B] = scalaz.Cokleisli(i.run)
  }
}

trait RWSTConverters extends MonadConversions {

  implicit def rwstAs[F[_], E, L, S, A](implicit F: cats.Monad[F]) =
    new AsScalaz[cats.data.RWST[F, E, L, S, A], scalaz.RWST[F, E, L, S, A]] with AsCats[scalaz.RWST[F, E, L, S, A], cats.data.RWST[F, E, L, S, A]] {

      def s2c(i: scalaz.RWST[F, E, L, S, A]): cats.data.RWST[F, E, L, S, A] = {
        cats.data.ReaderWriterStateT { (e, s) =>
          F.map(i.run(e, s)(monadToScalaz(Capture(F)))) {
            case (l, a, s) => (l, s, a)
          }
        }
      }

      def c2s(i: cats.data.RWST[F, E, L, S, A]): scalaz.RWST[F, E, L, S, A] =
        scalaz.ReaderWriterStateT((e, s) => F.map(i.run(e, s)) { case (l, s, a) => (l, a, s) })
    }
}
