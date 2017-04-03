# shims [![Build Status](https://travis-ci.org/djspiewak/shims.svg?branch=master)](https://travis-ci.org/djspiewak/shims) [![Gitter](https://img.shields.io/gitter/room/djspiewak/shims.svg)](https://gitter.im/djspiewak/shims) [![Maven Central](https://img.shields.io/maven-central/v/com.codecommit/shims-core_2.12.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.codecommit%22%20AND%20a%3A%22shims-core_2.12%22)

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

If you're using scala.js, use `%%%` instead.  Cross-builds are available for Scala 2.11 and 2.12.  It is *strongly* recommended that you enable the relevant SI-2712 fix in your build.  This can be done either by using [Typelevel Scala](https://github.com/typelevel/scala), adding [Miles Sabin's hacky compiler plugin](https://github.com/milessabin/si2712fix-plugin), or simply using Scala 2.12 or higher with the `-Ypartial-unification` flag.  An example of the shenanigans which can enable the SI-2712 fix across multiple Scala versions can be seen [here](https://github.com/djspiewak/shims/blob/34f8851d1726027b537707f27b6c33f83c15a9fd/build.sbt#L60-L91).  A large number of conversions will simply *not work* without partial unification.

Once you have the dependency installed, simply add the following import to any scopes which require cats-scalaz interop:

```scala
import shims._
```

*Chuckle*… there is no step three!

### Upstream Dependencies

- cats 0.9.0
- scalaz 7.2.10

At present, there is no complex build matrix of craziness to provide support for other major versions of each library.  This will probably come in time, when I've become sad and jaded, and possibly when I have received a pull request for it.

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

What follows is an alphabetized list (in terms of cats types) of typeclasses which are bidirectionally converted.  In all cases except where noted, the conversion is exactly as trivial as it seems.

- `Applicative`
- `Apply`
- `Choice`
  + Requires a `Bifunctor[F]` in addition to a `Choice[F]`.  This is because scalaz produces a `A \/ B`, while cats produces an `Either[A, B]`.
- `Bifoldable`
- `Bifunctor`
- `Bitraverse`
- `Category`
- `Choice`
- `CoflatMap`
- `Comonad`
- `Compose`
- `Contravariant`
- `Eq`
- `FlatMap`
  + Requires `Bind[F]` and *either* `BindRec[F]` *or* `Applicative[F]`.  This is because the cats equivalent of `scalaz.Bind` is actually `scalaz.BindRec`.  If an instance of `BindRec` is visible, it will be used to implement the `tailRecM` function.  Otherwise, a stack-*unsafe* `tailRecM` will be implemented in terms of `flatMap` and `point`.
  + The cats → scalaz conversion materializes `scalaz.BindRec`; there is no conversion which *just* materializes `Bind`.
- `Foldable`
- `Functor`
- `Invariant` (functor)
- `Monad`
  + Requires `Monad[F]` and *optionally* `BindRec[F]`.  Similar to `FlatMap`, this is because `cats.Monad` constrains `F` to define a `tailRecM` function, which may or may not be available on an arbitrary `scalaz.Monad`.  If `BindRec[F]` is available, it will be used to implement `tailRecM`.  Otherwise, a stack-*unsafe* `tailRecM` will be implemented in terms of `flatMap` and `point`.
  + The cats → scalaz conversion materializes `scalaz.Monad[F] with scalaz.BindRec[F]`, reflecting the fact that cats provides a `tailRecM`.
- `Monoid`
- `Order`
- `Profunctor`
- `Semigroup`
- `Split`
- `Strong`
- `Traverse`

### Datatypes

Datatype conversions are *explicit*, meaning that users must insert syntax which triggers the conversion.  In other words, there is no implicit coercion between data types: a method call is required.  For example, converting between `scalaz.Free` and `cats.free.Free` is done via the following:

```scala
val f1: scalaz.Free[F, A] = ???
val f2: cats.free.Free[F, A] = f1.asCats
val f3: scalaz.Free[F, A] = f2.asScalaz
```

| Cats                   | Scalaz                   |
| ---------------------- | ------------------------ |
| `scala.util.Either`    | `scalaz.\/`              |
| `cats.arrow.FunctionK` | `scalaz.~>`              |
| `cats.free.Free`       | `scalaz.Free`            |
| `cats.Eval`            | `scalaz.Free.Trampoline` |

Note that the `asScalaz`/`asCats` mechanism is open and extensible.  To enable support for converting some type "cats type" `A` to an equivalent "scalaz type" `B`, define an implicit instance of type `shims.conversions.AsScalaz[A, B]`.  Similarly, for some "scalaz type" `A` to an equivalent "cats type" `B`, define an implicit instance of type `shims.conversions.AsCats[A, B]`.  Thus, a pair of types, `A` and `B`, for which a bijection exists would have a single implicit instance extending `AsScalaz[A, B] with AsCats[B, A]` (though the machinery does not require this is handled with a *single* instance; the ambiguity resolution here is pretty straightforward).

## Previously, on shims…

Shims was previously (prior to version 1.0) a project for allowing middleware frameworks to avoid dependencies on *either* cats or scalaz, deferring that upstream decision to their downstream users.  It… didn't work very well, and nobody liked it.  Hence, its rebirth as a seamless interop framework!
