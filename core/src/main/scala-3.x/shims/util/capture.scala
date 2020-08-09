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

import shims.conversions.Synthetic

import scala.annotation.implicitNotFound
import scala.implicits.Not
import scala.util.Either

@implicitNotFound("could not find an implicit value of type ${A}")
final case class Capture[A](value: A) extends AnyVal

object Capture {

  implicit def materialize[A](implicit a: A, neg: Not[a.type <:< Synthetic]): Capture[A] =
    Capture(a)
}

@implicitNotFound("could not find an implicit value of type ${A} or ${B}")
final case class EitherCapture[A, B](value: Either[A, B]) extends AnyVal

private trait EitherLowPriorityImplicits {

  implicit def materializeLeft[A, B](implicit a: A, neg: Not[a.type <:< Synthetic]): EitherCapture[A, B] =
    EitherCapture(Left(a))
}

object EitherCapture extends EitherLowPriorityImplicits {

  implicit def materializeRight[A, B](implicit b: B, neg: Not[b.type <:< Synthetic]): EitherCapture[A, B] =
    EitherCapture(Right(b))
}

// this is defined for all A
final case class OptionCapture[A](value: Option[A]) extends AnyVal

private trait OptionLowPriorityImplicits {

  implicit def materializeNone[A]: OptionCapture[A] =
    OptionCapture(None)
}

object OptionCapture extends OptionLowPriorityImplicits {

  implicit def materializeSome[A](implicit a: A, neg: Not[a.type <:< Synthetic]): OptionCapture[A] =
    OptionCapture(Some(a))
}
