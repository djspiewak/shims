# Shims

[![Build Status](https://travis-ci.org/djspiewak/shims.svg?branch=master)](https://travis-ci.org/djspiewak/shims)

Once upon a time, [scalaz](https://github.com/scalaz/scalaz) was the standard library for functional and generally higher-order abstractions in the Scala community.  Library and framework authors could confidently write and publish functions in terms of `scalaz.Monad`, `scalaz.State` and so on.

Unfortunately, for various reasons, this is no longer the case.  [Cats](https://github.com/non/cats) is now on the scene, and many users may want to use it rather than scalaz in their downstream projects.  As a library author, we do not want to force a specific choice upon our users.  We would ideally like to supply our users with a single dependency that could work with scalaz *or* cats, without introducing classpath polution or code duplication.

Shims is a tool for achieving this goal.  It is *explicitly* targeted at upstream library authors, not downstream, user-facing projects!  If you have already committed to specifically scalaz or cats, or (more importantly) you have no reason to support both, **you do not need shims!**  However, if you are a library or framework author who does not want to force your downstream users into one universe or another, shims will solve all your problems.

The way this works is you divide your framework into (at minimum) three submodules: for example, *emm-core*, *emm-scalaz* and *emm-cats*.  You put all of your code into the *core* submodule, which has a dependency on *shims-core*.  You do *not* depend on scalaz or cats in your *core!*  When you need typeclasses (e.g. `Monad` or `Functor`), you use the types provided by shims (literally, `shims.Monad`).  Then, your *scalaz* and *cats* submodules are defined to depend on your *core* as well as *shims-scalaz* or *shims-cats*, respectively.  You can optionally define an `object scalaz extends shims.Implicits` (or analogously for cats), which insulates your end users from ever needing to know about shims.  This also gives you the ability to add custom shims in a uniform and user-transparent fashion.

From a user standpoint, they must add a single additional import which would not be required if you wrote specifically against cats *or* scalaz.  For example, for the emm project, the following *pair* of imports are required:

```scala
import emm._
import emm.compat.scalaz._
```

If emm were written specifically against scalaz, the second import would be unnecessary.  Users will also need to add a second SBT dependency on the *-scalaz* or *-cats* submodule of your project (e.g. *emm-scalaz*).  Transitive dependencies will take care of the rest!

Note that it is highly recommended that you use the following convention in your own projects if at all possible.  Or at least, it is recommended that you shy away from something like this:

```scala
// do NOT do this!
import emm._
import emm.scalaz._             // no no no no!
```

The problem with this revision (droping the `compat`) is hierarchical imports.  Importing `emm._` brings `scalaz` into scope, which masks the `_root_.scalaz` package, which is where all of the real scalaz stuff lives!  Tucking your compatibility objects off into their own package, which won't be independently imported by users, avoids this problem.

### SI-2712 Caveats

If you run Scala 2.12.1 (or Typelevel Scala) with the `-Ypartial-unification` flag, or if you use Miles Sabin's SI-2712 fix plugin with 2.11.8 or 2.10.6, you may see ambiguous implicits in *downstream* projects.  I haven't actually tried this yet, but it seems plausible.  If this happens, it's being (ironically!) caused by the series of redundant implicits required to work around SI-2712 if you *don't* have the fix enabled.  This is a known issue (well, known-ish), and should be resolved in a future minor release.

(**update**: it is resolved as of `0.4.1-cf0a86b`)

## SBT Setup

```sbt
libraryDependencies += "com.codecommit" %% "shims-core" % ShimsVersion
```

In your *-scalaz* and *-cats* subprojects, add the following dependencies:

```sbt
libraryDependencies += "com.codecommit" %% "shims-scalaz-71" % ShimsVersion        // for scalaz 7.1

// or!

libraryDependencies += "com.codecommit" %% "shims-scalaz-72" % ShimsVersion        // for scalaz 7.2

// or!

libraryDependencies += "com.codecommit" %% "shims-cats" % ShimsVersion        // for cats 0.9.0
```

Scala.js support (via `%%%`) is provided for every submodule except `scalaz-71`.

The current stable version of shims is **0.4**:

```sbt
val ShimsVersion = "0.4"
```

## Features

At present, the only non-typeclass feature which shims provides is a default right-projected syntax for `scala.util.Either`.  It provides this primarily in lieu of providing an `EitherLike`, which may change in the future.  The syntax also provides scalaz-style symbolic aliases for `Either`, so you can use it without tearing your hair out.  The syntax can be brought in by importing `shims.syntax.either._`

### Typeclasses

*TODO*

### Type Shapes

*TODO*

### Providing Instances

*TODO*

## What Shims is NOT

Shims is *not* a replacement for scalaz or cats!  It is not a competitor.  It does not fill the same needs.  If you think you need shims and you're not an upstream library author, chances are you actually need scalaz or cats.  Shims is a compatibility layer, nothing more.  If you're writing a "downstream" project (i.e. you deploy to a server, instead of to bintray/sonatype), you should absolutely not see "shims" in your SBT files.

## Contributing

This is a lazily-evaluated library.  Currently, it contains only just enough to make [emm](https://github.com/djspiewak/emm) and [http4s](http://http4s.org) operational.  If you need more than that, PRs are very much welcome.  Please note that this is a compatibility layer *specifically* for typeclasses!  It is not a replacement for scalaz *or* cats.  For example, we will not implement an `Xor` (or `\/`) delegate.  `State`, `Kleisli` and anything ending in `T` are similarly out of scope.  The whole point is just to write code which works with either cats or scalaz typeclasses, where they are equivalent.  I reserve the right to be pointlessly opinionated about what is and isn't out of scope.  Also, when we implement something, I reserve the right to be pointlessly opinionated about the names and types thereof.  So basically, welcome to my bikeshed.  :-)
