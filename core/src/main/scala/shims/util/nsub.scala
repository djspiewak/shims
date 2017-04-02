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

package shims.util

import scala.annotation.implicitNotFound

@implicitNotFound("unable to prove that $A is NOT a subtype of $B (likely because it actually is)")
sealed trait </<[A, B]

private[util] trait NSubLowPriorityImplicits {
  implicit def universal[A, B]: A </< B = null
}

object </< extends NSubLowPriorityImplicits {
  implicit def lie1[A, B](implicit ev: A <:< B): A </< B = null
  implicit def lie2[A, B](implicit ev: A <:< B): A </< B = null
}
