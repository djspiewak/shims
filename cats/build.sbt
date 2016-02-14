name := "shims-cats"

val CatsVersion = "0.4.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % CatsVersion,
  "org.typelevel" %% "cats-macros" % CatsVersion)