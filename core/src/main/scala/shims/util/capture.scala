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

package shims.util

import scala.language.experimental.macros

import scala.annotation.implicitNotFound
import scala.util.Either

@implicitNotFound("could not find an implicit value of type ${A}")
final case class Capture[A](value: A) extends AnyVal

object Capture {

  implicit def materialize[A]: Capture[A] =
    macro CaptureMacros.materializeCapture[A]
}

@implicitNotFound("could not find an implicit value of type ${A} or ${B}")
final case class EitherCapture[A, B](value: Either[A, B]) extends AnyVal

object EitherCapture {

  implicit def materialize[A, B]: EitherCapture[A, B] =
    macro CaptureMacros.materializeEitherCapture[A, B]
}

// this is defined for all A
final case class OptionCapture[A](value: Option[A]) extends AnyVal

object OptionCapture {

  implicit def materialize[A]: OptionCapture[A] =
    macro CaptureMacros.materializeOptionCapture[A]
}
