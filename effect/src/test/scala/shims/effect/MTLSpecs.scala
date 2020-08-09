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

package shims.effect

import cats.effect.{ContextShift, IO}
import cats.effect.laws.discipline.{arbitrary, AsyncTests, ConcurrentEffectTests, ConcurrentTests}, arbitrary._
import cats.effect.laws.util.{TestContext, TestInstances}, TestInstances._

import cats.{Eq, Functor, Monad}
import cats.instances.either._
import cats.instances.int._
import cats.instances.option._
import cats.instances.tuple._
import cats.instances.unit._
import cats.syntax.functor._

import scalaz.{EitherT, Kleisli, OptionT, StateT, WriterT}

import org.scalacheck.{Arbitrary, Prop}

import org.specs2.Specification
import org.specs2.scalacheck.Parameters
import org.specs2.specification.core.Fragments

import org.typelevel.discipline.Laws
import org.typelevel.discipline.specs2.Discipline

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import java.io.{ByteArrayOutputStream, PrintStream}

object MTLSpecs extends Specification with Discipline {

  def is =
    br ^ checkAllAsync("OptionT[IO, *]", implicit ctx => ConcurrentTests[OptionT[IO, *]].concurrent[Int, Int, Int]) ^
    br ^ checkAllAsync("Kleisli[IO, Int, *]", implicit ctx => ConcurrentTests[Kleisli[IO, Int, *]].concurrent[Int, Int, Int]) ^
    br ^ checkAllAsync("EitherT[IO, Throwable, *]", implicit ctx => ConcurrentEffectTests[EitherT[IO, Throwable, *]].concurrentEffect[Int, Int, Int]) ^
    br ^ checkAllAsync("StateT[IO, Int, *]", implicit ctx => AsyncTests[StateT[IO, Int, *]].async[Int, Int, Int]) ^
    br ^ checkAllAsync("WriterT[IO, Int, *]", implicit ctx => ConcurrentEffectTests[WriterT[IO, Int, *]].concurrentEffect[Int, Int, Int])

  def checkAllAsync(name: String, f: TestContext => Laws#RuleSet)(implicit p: Parameters) = {
    val context = TestContext()
    val ruleSet = f(context)

    Fragments.foreach(ruleSet.all.properties.toList) {
      case (id, prop) =>
        s"$name.$id" ! check(Prop(p => silenceSystemErr(prop(p))), p, defaultFreqMapPretty) ^ br
    }
  }

  implicit def iocsForEC(implicit ec: ExecutionContext): ContextShift[IO] =
    IO.contextShift(ec)

  implicit def optionTArbitrary[F[_], A](implicit arbFA: Arbitrary[F[Option[A]]]): Arbitrary[OptionT[F, A]] =
    Arbitrary(arbFA.arbitrary.map(OptionT.optionT(_)))

  implicit def kleisliArbitrary[F[_], R, A](implicit arbRFA: Arbitrary[R => F[A]]): Arbitrary[Kleisli[F, R, A]] =
    Arbitrary(arbRFA.arbitrary.map(Kleisli(_)))

  implicit def eitherTArbitrary[F[_]: Functor, L, A](implicit arbEA: Arbitrary[F[Either[L, A]]]): Arbitrary[EitherT[F, L, A]] =
    Arbitrary(arbEA.arbitrary.map(fe => EitherT.eitherT(fe.map(_.asScalaz))))

  implicit def stateTArbitrary[F[_]: Monad, S, A](implicit arbSFA: Arbitrary[S => F[(S, A)]]): Arbitrary[StateT[F, S, A]] =
    Arbitrary(arbSFA.arbitrary.map(StateT(_)))

  implicit def writerTArbitrary[F[_], L, A](implicit arbFLA: Arbitrary[F[(L, A)]]): Arbitrary[WriterT[F, L, A]] =
    Arbitrary(arbFLA.arbitrary.map(WriterT(_)))

  implicit def kleisliEq[F[_], A](implicit eqv: Eq[F[A]]): Eq[Kleisli[F, Int, A]] =
    Eq.by(_(42))   // totally random and comprehensive seed

  implicit def stateTEq[F[_]: Monad, S, A](implicit eqv: Eq[F[(Int, A)]]): Eq[StateT[F, Int, A]] =
    Eq.by(_.run(42))   // totally random and comprehensive seed

  // copied from cats-effect
  private def silenceSystemErr[A](thunk: => A): A = synchronized {
    // Silencing System.err
    val oldErr = System.err
    val outStream = new ByteArrayOutputStream()
    val fakeErr = new PrintStream(outStream)
    System.setErr(fakeErr)
    try {
      val result = thunk
      System.setErr(oldErr)
      result
    } catch {
      case NonFatal(e) =>
        System.setErr(oldErr)
        // In case of errors, print whatever was caught
        fakeErr.close()
        val out = outStream.toString("utf-8")
        if (out.nonEmpty) oldErr.println(out)
        throw e
    }
  }
}
