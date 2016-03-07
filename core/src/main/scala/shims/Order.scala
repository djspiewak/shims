package shims

trait Order[A] extends Equal[A] {

  /**
   * a1 < a2 ==> <0
   * a1 = a2 ==> =0
   * a1 > a2 ==> >0
   */
  def order(a1: A, a2: A): Int
}

object Order {
  type Aux[A, Tag0] = Order[A] { type Tag = Tag0 }

  def apply[A](implicit A: Order[A]) = A
}