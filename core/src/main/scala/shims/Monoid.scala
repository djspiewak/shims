package shims

trait Monoid[A] {
  def zero: A
  def append(a1: A, a2: => A): A
}