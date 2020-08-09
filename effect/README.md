# shims-effect [![Latest version](https://index.scala-lang.org/djspiewak/shims/shims/latest.svg*color=orange)](https://index.scala-lang.org/djspiewak/shims/shims)

This subproject defines [cats-effect](https://typelevel.org/cats-effect/) instances for scalaz datatypes. Particularly useful if you're trying to use a project like [fs2](https://github.com/functional-streams-for-scala/fs2) or [http4s](https://http4s.org) with older scalaz code which is still using `Task` or (😱) `scalaz.effect.IO`.

## Usage

```sbt
libraryDependencies += "com.codecommit" %% "shims-effect" % "<version>"
```

Unfortunately, is no scala.js build for shims-effect due to the fact that scalaz-effect and scalaz-concurrent are only published for the JVM.

With this dependency replacing your conventional `"com.codecommit" %% "shims"` dependency, you can replace your `import shims._` with the following:

```scala
import shims.effect._
```

This *replaces* your `shims._` import, it does not augment it! This is very important. **Do not do this:**

```scala
import shims._         // no no no!
import shims.effect._  // no no no!
```

No no no no...

**One or the other, not both.**

The reason for this is implicit ambiguity. `Effect` is a subtype of `cats.Monad`. This means that, if we just naively define an `Effect[Task]` in a scope which also has `import shims._`, then the `Monad[Task]` which we get through `Effect` is ambiguous with the `Monad` which is automatically materialized from the `scalaz.Monad[Task]` instance. This is also why these instances are being provided by shims, instead of by a third-party project (as it was originally).

### Upstream Dependencies

- Obviously, the core **shims** subproject
- cats-effect 2.0.0
- scalaz-concurrent 7.2.28

## Conversions

### Instances

- `Effect[scalaz.concurrent.Task]`
- `Parallel[scalaz.concurrent.Task]`
- `Sync[scalaz.effect.IO]`

Don't use `scalaz.effect.IO`. Honestly. It's strictly worse than every other option out there. Search/replace your imports today.

#### MTL

Just as cats-effect provides lifted versions of its core typeclasses (`Sync`, `Async`, etc) for the cats monad transformers, so to does shims-effect provide such lifted instances for the scalaz monad transformers. Specifically:

- `OptionT`
  + `ContextShift`
  + `LiftIO`
  + `Sync`
  + `Async`
  + `Concurrent`
- `Kleisli`
  + `ContextShift`
  + `LiftIO`
  + `Sync`
  + `Async`
  + `Concurrent`
- `EitherT`
  + `ContextShift`
  + `LiftIO`
  + `Sync`
  + `Async`
  + `Concurrent`
  + `Effect` (for `L = Throwable`)
  + `ConcurrentEffect` (for `L = Throwable`)
- `StateT`
  + `ContextShift`
  + `LiftIO`
  + `Sync`
  + `Async`
- `WriterT` (given `Monoid[W]`)
  + `ContextShift`
  + `LiftIO`
  + `Sync`
  + `Async`
  + `Concurrent`
  + `Effect`
  + `ConcurrentEffect`

You'll notice that several `StateT`-related instances are missing (specifically: `Concurrent`, `Effect`, and `ConcurrentEffect` given `Monoid[S]`). This is intentional. These instances are highly dubious and I never should have added them to cats-effect.

As a caveat, `scalaz.StateT` is *not* stack safe under repeated `bind`s. This is problematic because all `Sync`s are assumed to be stack-safe. Be very careful with this instance. It is included mostly because it's quite useful, but do not assume you can run arbitrarily long computational chains in the way that you can with `cats.StateT`.

### Typeclasses

- `scalaz.effect.LiftIO`
  + Requires a `cats.effect.LiftIO[F]`. Note that this conversion does *not* go in the other direction!

### Why Not More*

This is sort of a quick FAQ on why this subproject doesn't do *more* than it currently does:

- **Why not `ConcurrentEffect[Task]`?**
  + Because it's unsound. `Task` doesn't provide particularly robust or safe cancellation support, and it has absolutely no notion of guaranteed finalization. Therefore, it would not be able to lawfully implement the functions in `Concurrent`. Yes, this does mean that there are certain things you cannot do with `Task`, but they wouldn't be safe anyway.
  + As an aside, it is *theoretically* possible to do this, but it would require reimplementing a lot of the safe preemption mechanisms which are built into `cats.effect.IO`, and frankly I just don't feel like there's a particular need for this. Just don't use `Task` if you can avoid it.
- **Why not materialize `cats.effect.LiftIO`?**
  + Because it's unsound (this is a common theme). There's really no way to convert a `cats.effect.IO` to a `scalaz.effect.IO` without blocking a thread, due to the `async` constructor. We can go in the other direction only because `scalaz.effect.IO` is strictly less powerful than `cats.effect.IO`.
- **Why not do something with `scalaz.Nondeterminism`?**
  + Mostly because it's kind of useless. It seems *intuitive* that we could bidirectionally coerce it to a `cats.Parallel`, but this doesn't work broadly because `Parallel` is a sound concept and `Nondeterminism` is not. `Nondeterminism` exposes a very `Applicative`-like API with a name that isn't "applicative", which sounds a lot like `Parallel` except for the fact that it forces you to use the same type constructor for which you're also (presumably) exposing a `Monad` and such. This is at the very least unsound *in principle*, since `Applicative` and `Monad` must be consistent by the laws, and it is most definitely dangerous in practice. To make matters worse, `Nondeterminism` wraps a ton of very unsafe race functions around this interface. These functions are broken [on a fundamental level](https://gist.github.com/djspiewak/a775b73804c581f4028fea2e98482b3c) and should never be used for any reason.
- **Is there value in `ST`, `MVar`, or the other goodies in `scalaz.effect` or `scalaz.concurrent`?**
  + Nope. At least, not from the standpoint of shims. `MVar` blocks and should never be used under any circumstances (if you want something like `MVar`, use `cats.effect.concurrent.Ref`). `ST` is quite cool, but its practical utility seems limited given that we can just call `Sync[F].delay(...)`. `RegionT` is also cool, but about 95% of its practical functionality is superseded in a more convenient package by the resource management in fs2.
- **What about `scalaz.effect.MonadIO`?**
  + Naively-encoded MTL-style typeclasses don't work particularly well in Scala due to the fact that the compiler doesn't enforce coherence. There are two currently-accepted solutions to this problem: either split materializations (as in [cats-mtl](https://github.com/typelevel/cats-mtl)) so users are forced to be explicit about the fact that they have a `Monad[F]` *and* a `MonadIO[F]`, or encode an implicit disambiguation lattice at the type level (as in [scato](https://github.com/aloiscochard/scato) and scalaz 8). Both solutions work; neither are compatible with `scalaz.effect.MonadIO`.
