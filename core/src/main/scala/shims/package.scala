package object shims
    extends conversions.MonadConversions
    with conversions.OrderConversions
    with conversions.BitraverseConversions
    with conversions.EitherConversions {

  implicit final class AsSyntax[A](val self: A) extends AnyVal {
    def asScalaz[B](implicit A: conversions.AsScalaz[A, B]): B = A.c2s(self)
    def asCats[B](implicit A: conversions.AsCats[A, B]): B = A.s2c(self)
  }
}
