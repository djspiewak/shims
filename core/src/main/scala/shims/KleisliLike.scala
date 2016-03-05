package shims

trait KleisliLike[K[_[_], _, _]] {

  def apply[M[_], A, B](run: A => M[B]): K[M, A, B]
}
