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
import scala.util.{Either, Left, Right}

@implicitNotFound("unable to find an implicit value of type ${A}")
final case class Capture[A, T](value: A) extends AnyVal

object Capture {
  implicit def materialize[A <: AnyRef](implicit A: A): Capture[A, A.type] = Capture(A)
}

@implicitNotFound("unable to find an implicit value of type ${A} or ${B}")
final case class EitherCapture[A, B, T](value: Either[A, B]) extends AnyVal

trait EitherLowPriorityImplicits {

  implicit def materializeLeft[A <: AnyRef, B](implicit A: A): EitherCapture[A, B, A.type] =
    EitherCapture(Left(A))
}

object EitherCapture extends EitherLowPriorityImplicits {

  implicit def materializeRight[A, B <: AnyRef](implicit B: B): EitherCapture[A, B, B.type] =
    EitherCapture(Right(B))
}

// this is defined for all A
final case class OptionCapture[A, T](value: Option[A]) extends AnyVal

trait OptionLowPriorityImplicits {

  implicit def materializeNone[A]: OptionCapture[A, Any] =
    OptionCapture(None)
}

object OptionCapture extends OptionLowPriorityImplicits {

  implicit def materializeSome[A <: AnyRef](implicit A: A): OptionCapture[A, A.type] =
    OptionCapture(Some(A))
}
