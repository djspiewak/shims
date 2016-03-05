name := "shims-core"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.7.2" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")