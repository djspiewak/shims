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

import cats.arrow.FunctionK

import shims.util.Capture

trait InjectConversions {

  private[conversions] trait InjectShimS2C[F[_], G[_]] extends cats.InjectK[F, G] with Synthetic {
    val FG: scalaz.Inject[F, G]

    override def inj: FunctionK[F, G] = λ[FunctionK[F, G]](FG.inj(_))
    override def prj: FunctionK[G, λ[α => Option[F[α]]]] = λ[FunctionK[G, λ[α => Option[F[α]]]]](FG.prj(_))
  }

  implicit def injectToCats[F[_], G[_]](implicit FC: Capture[scalaz.Inject[F, G]]): cats.InjectK[F, G] with Synthetic =
    new InjectShimS2C[F, G] { val FG = FC.value }

  // we can't go in the other direction because of limitations in scalaz
}
