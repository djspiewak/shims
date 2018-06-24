## Extending shims

Under certain circumstances, you may wish to have some additional custom conversions in the style of shims which dovetail into the same implicit prioritization mechanism (e.g. so your custom `Monad` instance doesn't conflict with the one that shims is trying to automatically materialize). An obvious example of this is the **shims-effect** subproject, but it's easy to imagine third-party projects which may have similar needs.

This kind of extension can be achieved by mixing in the `shims.ShimsCore` trait into your top-level conversion import and then explicitly declaring dependencies by mixing in the specific conversion traits as necessary (e.g. `shims.conversions.MonadErrorConversions`). You can see an example of this in shims-effect:

- [`shims.effect` package object](https://github.com/djspiewak/shims/blob/master/effect/src/main/scala/shims/effect/package.scala)
- [`ShimsEffect` top-level trait](https://github.com/djspiewak/shims/blob/master/effect/src/main/scala/shims/effect/ShimsEffect.scala)
- [`TaskInstances` conversions](https://github.com/djspiewak/shims/blob/master/effect/src/main/scala/shims/effect/instances/TaskInstances.scala)

Mixing `ShimsCore` into `ShimsEffect`, which is subsequently extended by the `shims.effect` package object, is what brings in the *entire* set of core shims coercions, with appropriate prioritization. Mixing in `MonadErrorConversions` into `TaskInstances` ensures that the `taskEffect` definition has higher priority than the automatically-materialized `MonadError` instances in shims.
