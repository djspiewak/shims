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

// topological root(s): EitherConverters, FunctionKConverters, FreeConverters
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
