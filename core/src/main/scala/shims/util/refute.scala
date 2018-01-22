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

@implicitNotFound("Could not refute ${A}. An implicit instance was found.")
sealed trait Refute[A]
object Refute {
  private val instance = new Refute[Any] { }
  implicit def refute[A]: Refute[A] = instance.asInstanceOf[Refute[A]]
  implicit def ambig[A](implicit ev: A): Refute[A] = ???
}
