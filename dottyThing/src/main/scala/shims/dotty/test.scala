package shims

import cats.kernel.Eq
import cats.instances.list._

object Test {
  implicit val eqStr: Eq[String] = null

  Eq[String]
  Eq[List[String]]
}
