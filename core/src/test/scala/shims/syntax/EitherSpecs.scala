package shims.syntax

import org.specs2.mutable._

object EitherSpecs extends Specification {
  import either._

  "fancy either syntax" should {
    "allow creation of left/right" in {
      \/-(42) should beRight(42)
      -\/(42) should beLeft(42)
    }

    "right-project map" in {
      val e: String \/ Int = \/-(42)
      e map (2 *) must beRight(84)
    }

    "right-project flatMap" in {
      val e: String \/ Int = \/-(42)

      e flatMap { i => \/-(i * 2) } must beRight(84)
      e flatMap { i => -\/(i.toString) } must beLeft("42")
    }
  }
}