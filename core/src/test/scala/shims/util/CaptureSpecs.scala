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

import org.specs2.mutable._

object CaptureSpecs extends Specification {

  "capture" should {
    trait Foo
    implicit case object Foo extends Foo

    "find implicits" in {
      implicitly[Capture[Foo]].value mustEqual implicitly[Foo]
    }
  }

  "either capture" should {
    "find left" in {
      trait Foo
      implicit case object Foo extends Foo
      trait Bar

      implicitly[EitherCapture[Foo, Bar]].value must beLeft(implicitly[Foo])
    }

    "find right" in {
      trait Foo
      trait Bar
      implicit case object Bar extends Bar

      implicitly[EitherCapture[Foo, Bar]].value must beRight(implicitly[Bar])
    }
  }

  "option capture" should {
    "find some" in {
      trait Foo
      implicit case object Foo extends Foo

      implicitly[OptionCapture[Foo]].value must beSome(implicitly[Foo])
    }

    "find none" in {
      trait Foo

      implicitly[OptionCapture[Foo]].value must beNone
    }
  }
}
