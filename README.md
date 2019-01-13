# shims [![Build Status](https://travis-ci.org/djspiewak/shims.svg?branch=master)](https://travis-ci.org/djspiewak/shims) [![Gitter](https://img.shields.io/gitter/room/djspiewak/shims.svg)](https://gitter.im/djspiewak/shims) [![Latest version](https://index.scala-lang.org/djspiewak/shims/shims/latest.svg?color=orange)](https://index.scala-lang.org/djspiewak/shims/shims)

Shims aims to provide a convenient, bidirectional, and transparent set of conversions between scalaz and cats, covering typeclasses (e.g. `Monad`) and data types (e.g. `\/`).  By that I mean, with shims, anything that has a `cats.Functor` instance also has a `scalaz.Functor` instance, *and vice versa*.  Additionally, every convertible scalaz datatype – such as `scalaz.State` – has an implicitly-added `asCats` function, while every convertible cats datatype – such as `cats.free.Free` – has an implicitly-added `asScalaz` function.

Only a single import is required to enable any and all functionality:

```scala
import shims._
```

Toss that at the top of any files which need to work with APIs written in terms of both frameworks, and everything should behave seamlessly.  You can see some examples of this in the test suite, where we run the cats laws-based property tests on *scalaz* instances of various typeclasses.

## Usage

Add the following to your SBT configuration:

```sbt
libraryDependencies += "com.codecommit" %% "shims" % "<version>"
```

If you're using scala.js, use `%%%` instead.  Cross-builds are available for Scala 2.11, 2.12, and 2.13-M4.  It is *strongly* recommended that you enable the relevant SI-2712 fix in your build.  [Details here](https://github.com/typelevel/cats/tree/b23c7fbc117856910fa43de205457d8637eef8c6#getting-started).  A large number of conversions will simply *not work* without partial unification.

Once you have the dependency installed, simply add the following import to any scopes which require cats-scalaz interop:

```scala
import shims._
```

*Chuckle*… there is no step three!

### Effect Types

You can also use shims to bridge the gap between the older scalaz `Task` hierarchy and newer frameworks which assume [cats-effect](https://typelevel.org/cats-effect/) typeclasses and similar:

```sbt
libraryDependencies += "com.codecommit" %% "shims-effect" % "<version>"
```

```scala
import shims.effect._
```

For more information, see the [**shims-effect** subproject readme](effect/README.md).

### Upstream Dependencies

- cats 1.4.0
- scalaz 7.2.26

At present, there is no complex build matrix of craziness to provide support for other major versions of each library.  This will probably come in time, when I've become sad and jaded, and possibly when I have received a pull request for it.

### Quick Example

In this example, we build a data structure using both scalaz's `IList` and cats' `Eval`, and then we use the *cats* `Traverse` implicit syntax, which necessitates performing multiple transparent conversions.  Then, at the end, we convert the cats `Eval` into a scalaz `Trampoline` using the explicit `asScalaz` converter.

```scala
import shims._

import cats.Eval
import cats.syntax.traverse._
import scalaz.{IList, Trampoline}

val example: IList[Eval[Int]] = IList(Eval.now(1), Eval.now(2), Eval.now(3))

val sequenced: Eval[IList[Int]] = example.sequence
val converted: Trampoline[IList[Int]] = sequenced.asScalaz
```

## Conversions

### Typeclasses

Typeclass conversions are *transparent*, meaning that they will materialize fully implicitly without any syntactic interaction.  Effectively, this means that all cats monads are scalaz monads *and vice versa*.

What follows is an alphabetized list (in terms of cats types) of typeclasses which are bidirectionally converted.  In all cases except where noted, the conversion is exactly as trivial as it seems.

- `Alternative`
  + Note that `MonadPlus` doesn't exist in Cats. I'm not sure if this is an oversight. At present, no conversions are attempted, even when `Alternative` and `FlatMap` are present for a given `F[_]`. Change my mind.
- `Applicative`
- `Apply`
- `Arrow`
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
- `Distributive`
- `Eq`
- `FlatMap`
  + Requires `Bind[F]` and *either* `BindRec[F]` *or* `Applicative[F]`.  This is because the cats equivalent of `scalaz.Bind` is actually `scalaz.BindRec`.  If an instance of `BindRec` is visible, it will be used to implement the `tailRecM` function.  Otherwise, a stack-*unsafe* `tailRecM` will be implemented in terms of `flatMap` and `point`.
  + The cats → scalaz conversion materializes `scalaz.BindRec`; there is no conversion which *just* materializes `Bind`.
- `Foldable`
- `Functor`
- `InjectK`
  + This conversion is weird, because we can materialize a `cats.InjectK` given a `scalaz.Inject`, but we cannot go in the other direction because `scalaz.Inject` is sealed.
- `Invariant` (functor)
- `Monad`
  + Requires `Monad[F]` and *optionally* `BindRec[F]`.  Similar to `FlatMap`, this is because `cats.Monad` constrains `F` to define a `tailRecM` function, which may or may not be available on an arbitrary `scalaz.Monad`.  If `BindRec[F]` is available, it will be used to implement `tailRecM`.  Otherwise, a stack-*unsafe* `tailRecM` will be implemented in terms of `flatMap` and `point`.
  + The cats → scalaz conversion materializes `scalaz.Monad[F] with scalaz.BindRec[F]`, reflecting the fact that cats provides a `tailRecM`.
- `MonadError`
  + Similar requirements to `Monad`
- `Monoid`
- `MonoidK`
- `Order`
- `Profunctor`
- `Semigroup`
- `SemigroupK`
- `Show`
  + The cats → scalaz conversion requires a `Show.ContravariantShow` (which is the supertype of `Show`), just for extra flexibility. This should be invisible to users 99% of the time.
- `Strong`
- `Traverse`

Note that some typeclasses exist in one framework but not in the other (e.g. `Group` in cats, or `Split` in scalaz).  In these cases, no conversion is attempted, though practical conversion may be achieved through more specific instances (e.g. `Arrow` is a subtype of `Split`, and `Arrow` will convert).

And don't get me started on the whole `Bind` vs `BindRec` mess.  I make no excuses for that conversion.  Just trying to make things work as reasonably as possible, given the constraints of the upstream frameworks.

Let me know if I missed anything!  Comprehensive lists of typeclasses in either framework are hard to come by.

### Datatypes

Datatype conversions are *explicit*, meaning that users must insert syntax which triggers the conversion.  In other words, there is no implicit coercion between data types: a method call is required.  For example, converting between `scalaz.Free` and `cats.free.Free` is done via the following:

```scala
val f1: scalaz.Free[F, A] = ???
val f2: cats.free.Free[F, A] = f1.asCats
val f3: scalaz.Free[F, A] = f2.asScalaz
```

| Cats                      | Direction | Scalaz                   |
| ------------------------- | :-------: | ------------------------ |
| `cats.Eval`               | 👈👉      | `scalaz.Free.Trampoline` |
| `cats.Eval`               | 👈        | `scalaz.Name`            |
| `cats.Eval`               | 👈        | `scalaz.Need`            |
| `cats.Eval`               | 👈        | `scalaz.Value`           |
| `cats.arrow.FunctionK`    | 👈👉      | `scalaz.~>`              |
| `cats.data.Cokleisli`     | 👈👉      | `scalaz.Cokleisli`       |
| `cats.data.Const`         | 👈👉      | `scalaz.Const`           |
| `cats.data.EitherK`       | 👈👉      | `scalaz.Coproduct`       |
| `cats.data.EitherT`       | 👈👉      | `scalaz.EitherT`         |
| `cats.data.IndexedStateT` | 👈👉      | `scalaz.IndexedStateT`   |
| `cats.data.Ior`           | 👈👉      | `scalaz.\&/`             |
| `cats.data.Kleisli`       | 👈👉      | `scalaz.Kleisli`         |
| `cats.data.NonEmptyList`  | 👈👉      | `scalaz.NonEmptyList`    |
| `cats.data.OneAnd`        | 👈👉      | `scalaz.OneAnd`          |
| `cats.data.OptionT`       | 👈👉      | `scalaz.OptionT`         |
| `cats.data.OptionT`       | 👈        | `scalaz.MaybeT`          |
| `cats.data.RWST`          | 👈👉      | `scalaz.RWST`            |
| `cats.data.Validated`     | 👈👉      | `scalaz.Validation`      |
| `cats.data.ValidatedNel`  | 👈👉      | `scalaz.ValidationNel`   |
| `cats.data.WriterT`       | 👈👉      | `scalaz.WriterT`         |
| `cats.free.Free`          | 👈👉      | `scalaz.Free`            |
| `scala.Option`            | 👈        | `scalaz.Maybe`           |
| `scala.util.Either`       | 👈👉      | `scalaz.\/`              |

Note that the `asScalaz`/`asCats` mechanism is open and extensible.  To enable support for converting some type "cats type" `A` to an equivalent "scalaz type" `B`, define an implicit instance of type `shims.conversions.AsScalaz[A, B]`.  Similarly, for some "scalaz type" `A` to an equivalent "cats type" `B`, define an implicit instance of type `shims.conversions.AsCats[A, B]`.  Thus, a pair of types, `A` and `B`, for which a bijection exists would have a single implicit instance extending `AsScalaz[A, B] with AsCats[B, A]` (though the machinery does not require this is handled with a *single* instance; the ambiguity resolution here is pretty straightforward).

Wherever extra constraints are required (e.g. the various `StateT` conversions require a `Monad[F]`), the converters require the *cats* variant of the constraint.  This should be invisible under normal circumstances since shims itself will materialize the other variant if one is available.

### Nesting

At present, the `asScalaz`/`asCats` mechanism does not recursively convert nested structures.  This situation most commonly occurs with monad transformer stacks.  For example:

```scala
val stuff: EitherT[OptionT[Foo, ?], Errs, Int] = ???

stuff.asCats
```

The type of the final line is `cats.data.EitherT[scalaz.OptionT[Foo, ?], Errs, Int]`, whereas you might *expect* that it would be `cats.data.EitherT[cats.data.OptionT[Foo, ?], Errs, Int]`.  It is technically possible to apply conversions in depth, though it require some extra functor constraints in places.  The primary reason why this isn't done (now) is compile time performance, which would be adversely affected by the non-trivial inductive solution space.

It shouldn't be too much of a hindrance in any case, since the typeclass instances for the nested type will be materialized for both scalaz and cats, and so it doesn't matter as much *exactly* which nominal structure is in use.  It would really only matter if you had a function which explicitly expected one thing or another.

The only exception to this rule is `ValidationNel` in scalaz and `ValidatedNel` in cats.  Converting this composite type is a very common use case, and thus an specialized converter is defined:

```scala
val v: ValidationNel[Errs, Int] = ???

v.asCats   // => v2: ValidatedNel[Errs, Int]
```

Note that the `scalaz.NonEmptyList` within the `Validation` was converted to a `cats.data.NonEmptyList` within the resulting `Validated`.

In other words, under normal circumstances you will need to manually map nested structures in order to deeply convert them, but `ValidationNel`/`ValidatedNel` will Just Work™ without any explicit induction.

## Contributors

None of this would have been possible without some really invaluable assistance:

- Guillaume Martres ([@smarter](https://github.com/smarter)), who provided the key insight into the `scalac` bug which was preventing the implementation of `Capture` (and thus, bidirectional conversions)
- Christopher Davenport ([@ChristopherDavenport](https://github.com/ChristopherDavenport)), who contributed the bulk of **shims-effect** in its original form on **scalaz-task-effect**
