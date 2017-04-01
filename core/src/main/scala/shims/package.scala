import shims.conversions._

package object shims
    extends MonadConversions
    with OrderConversions
    with BitraverseConversions
    with ArrowConversions
    with ChoiceConversions
    with EitherConversions {

  implicit final class AsSyntax[A](val self: A) extends AnyVal {
    def asScalaz[B](implicit A: AsScalaz[A, B]): B = A.c2s(self)
    def asCats[B](implicit A: AsCats[A, B]): B = A.s2c(self)
  }
}
