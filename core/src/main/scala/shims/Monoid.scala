package shims

trait Monoid[A] {
  type Tag

  def zero: A
  def append(a1: A, a2: => A): A
}

object Monoid {
  type Aux[A, Tag0] = Monoid[A] { type Tag = Tag0 }

  def apply[A](implicit A: Monoid[A]) = A
}