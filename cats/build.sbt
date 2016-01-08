name := "shims-cats"

val CatsVersion = "0.3.0"

libraryDependencies ++= Seq(
  "org.spire-math" %% "cats-core" % CatsVersion,
  "org.spire-math" %% "cats-macros" % CatsVersion)