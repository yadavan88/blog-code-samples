name := "blog-code-samples"

lazy val cats = project
  .in(file("cats"))
  .settings(
    name := "cats",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core" % "2.8.0",
        "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )

lazy val scala3 = project
  .in(file("scala3"))
  .settings(
    name := "scala3",
    scalaVersion := "3.2.0"
  )

 