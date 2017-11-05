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

import shims.conversions._

package object shims
    extends MonadErrorConversions
    with MonoidConversions
    with BitraverseConversions
    with ChoiceConversions
    with InjectConversions
    with EitherConverters
    with FunctionKConverters
    with EvalConverters
    with IndexedStateTConverters
    with EitherKConverters
    with KleisliConverters
    with OptionTConverters
    with ValidatedNELConverters
    with OneAndConverters
    with MaybeConverters
    with MaybeTConverters
    with WriterTConverters
    with IorConverters
    with ConstConverters
    with CokleisliConverters {

  implicit final class AsSyntax[A](val self: A) extends AnyVal {
    def asScalaz[B](implicit A: AsScalaz[A, B]): B = A.c2s(self)
    def asCats[B](implicit A: AsCats[A, B]): B = A.s2c(self)
  }
}
