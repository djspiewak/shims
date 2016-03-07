package shims

trait Show[A] {
  type Tag

  def show(a: A): String
}

object Show {
  type Aux[A, Tag0] = Show[A] { type Tag = Tag0 }

  def apply[A](implicit A: Show[A]) = A
}