# shims

[![Build Status](https://travis-ci.org/djspiewak/shims.svg?branch=master)](https://travis-ci.org/djspiewak/shims)

Shims aims to provide a convenient, bidirectional and transparent set of conversions between scalaz and cats, covering typeclasses (e.g. `Monad`) and data types (e.g. `\/`).  By that I mean, with shims, anything that has a `cats.Functor` instance also has a `scalaz.Functor` instance, *and vice versa*.  Additionally, every convertable scalaz datatype – such as `scalaz.State` – has an implicitly-added `asCats` function, while every convertable cats datatype – such as `cats.free.Free` – has an implicitly-added `asScalaz` function.  Only a single import is required to enable any and all functionality:

```scala
import shims._
```

Toss that at the top of any files which need to work with APIs written in terms of both frameworks, and everything should behave seamlessly.  You can see some examples of this in the test suite, where we run the cats laws-based property tests on *scalaz* instances of various typeclasses.

## Usage

Add the following to your SBT configuration:

```sbt
libraryDependencies += "com.codecommit" %% "shims-core" % "1.0"
```

There is currently no *stable* released version of shims 1.0 (the only stable releases represent the prior library state).  If you want to live dangerously, I've published a hash snapshot with version `"1.0-b0e5152"`.

If you're using scala.js, use `%%%` instead.  Cross-builds are available for Scala 2.11 and 2.12.  It is *strongly* recommended that you enable the relevant SI-2712 fix in your build.  This can be done either by using Typelevel Scala, adding Miles Sabin's hacky compiler plugin, or simply using Scala 2.12 or higher with the `-Ypartial-unification` flag.  A large number of conversions will simply *not work* without partial unification.

Once you have the dependency installed, simply add the following import to any scopes which require cats-scalaz interop:

```scala
import shims._
```

*Chuckle*… there is no step three!

### Common Issues

If you get a "diverging implicit expansion" error, it *probably* means that you simply didn't have the appropriate upstream implicit in scope.  For example, consider the following:

```scala
import cats.kernel.Eq

import scalaz.std.anyVal._
import scalaz.std.option._

Eq[(Int, Int)]       // error!
```

The above will produce a diverging implicit expansion error.  The reasons for this are… complicated.  But the problem is actually simple: we're missing an implicit declaration for how to apply `scalaz.Equal` to `Tuple2`!  We would get a more informative error if we had tried to summon `scalaz.Equal[(Int, Int)]`, but in either case, the solution is identical: add the appropriate import.

```scala
import cats.kernel.Eq

import scalaz.std.anyVal._
import scalaz.std.option._
import scalaz.std.tuple._

Eq[(Int, Int)]       // works!
```

So when in doubt, if you get an error summoning a cats/scalaz typeclass converted from the presence of the other, try to summon the other implicitly and see what happens.  We got a weird error trying to summon an implicitly materialized cats instance from a scalaz instance, and we were able to debug the issue by trying to summon the "natural" scalaz instance.

## Conversions

### Typeclasses

Typeclass conversions are *transparent*, meaning that they will materialize fully implicitly without any syntactic interaction.  Effectively, this means that all cats monads are scalaz monads *and vice versa*.

*TODO*

### Datatypes

Datatype conversions are *explicit*, meaning that users must insert syntax which triggers the conversion.  In other words, there is no implicit coercion between data types: a method call is required.  For example, converting between `scalaz.Free` and `cats.free.Free` is done via the following:

```scala
val f1: scalaz.Free[F, A] = ???
val f2: cats.free.Free[F, A] = f1.asCats
```

*TODO*

## Previously, on shims…

Shims was previously (prior to version 1.0) a project for allowing middleware frameworks to avoid dependencies on *either* cats or scalaz, deferring that upstream decision to their downstream users.  It… didn't work very well, and nobody liked it.  Hence, its rebirth as a seamless interop framework!
