/*
 * Copyright 2017 Daniel Spiewak
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

import scalaz.{~>, \/}
import cats.arrow.FunctionK

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
      f.foldMap[scalaz.Free[S, ?]](λ[FunctionK[S, scalaz.Free[S, ?]]](scalaz.Free.liftF(_)))

    def s2c(f: scalaz.Free[S, A]) =
      f.foldMap[cats.free.Free[S, ?]](λ[S ~> cats.free.Free[S, ?]](cats.free.Free.liftF(_)))
  }
}

trait EvalConverters extends FreeConverters {
  import cats.Eval
  import scalaz.Trampoline
  import scalaz.Free.{Trampoline => FT}

  implicit def evalAs[A] = new AsScalaz[Eval[A], FT[A]] with AsCats[FT[A], Eval[A]] {

    // the inner workings of eval aren't exposed, so we can't do any better here
    def c2s(e: Eval[A]) = Trampoline.delay(e.value)

    def s2c(t: FT[A]) = t.foldMap(λ[Function0 ~> Eval](a => Eval.always(a())))
  }
}

trait StateTConverters extends MonadConversions {

  implicit def stateTAs[F[_]: cats.Monad, S, A] =
    new AsScalaz[cats.data.StateT[F, S, A], scalaz.StateT[F, S, A]] with AsCats[scalaz.StateT[F, S, A], cats.data.StateT[F, S, A]] {

      def c2s(st: cats.data.StateT[F, S, A]) =
        scalaz.StateT[F, S, A](s => cats.Monad[F].flatMap(st.runF)(_(s)))

      def s2c(st: scalaz.StateT[F, S, A]) =
        cats.data.StateT[F, S, A](st.run(_))
    }
}

trait NELConverters {

  implicit def nelAs[A] = new AsScalaz[cats.data.NonEmptyList[A], scalaz.NonEmptyList[A]] with AsCats[scalaz.NonEmptyList[A], cats.data.NonEmptyList[A]] {

    def c2s(nel: cats.data.NonEmptyList[A]) = scalaz.NonEmptyList(nel.head, nel.tail: _*)
    def s2c(nel: scalaz.NonEmptyList[A]) = cats.data.NonEmptyList(nel.head, nel.tail.toList)
  }
}
