name := "blog-code-samples"

val scala2Version = "2.13.10"

lazy val cats = project
  .in(file("cats"))
  .settings(
    name := "cats",
    scalaVersion := scala2Version,
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

lazy val scala2 = project
  .in(file("scala2"))
  .settings(
    name := "scala2",
    scalaVersion := scala2Version
  )
