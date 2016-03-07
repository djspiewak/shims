package shims

trait Equal[A] {
  type Tag

  def equal(a1: A, a2: A): Boolean
}

object Equal {
  type Aux[A, Tag0] = Equal[A] { type Tag = Tag0 }

  def apply[A](implicit A: Equal[A]) = A
}