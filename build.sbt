name := "blog-code-samples"

val scala2Version = "2.13.10"

lazy val cats = project
  .in(file("cats"))
  .settings(
    name := "cats",
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
val diffxVersion = "0.8.3"

lazy val scala3 = project
  .in(file("scala3"))
  .settings(
    name := "scala3",
    scalaVersion := "3.2.2",
    libraryDependencies ++= Seq(
      "com.softwaremill.diffx" %% "diffx-core" % diffxVersion,
      "org.scalatest" %% "scalatest-flatspec" % "3.2.15" % Test
    )
  )

val testContainersVersion = "0.40.15"

lazy val scala2 = project
  .in(file("scala2"))
  .settings(
    name := "scala2",
    scalaVersion := scala2Version,
    Test / fork := true,
    libraryDependencies ++= Seq(
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainersVersion % Test,
      "org.tpolecat" %% "skunk-core" % "0.5.1",
      "org.scalatest" %% "scalatest-flatspec" % "3.2.15" % Test,
      "org.postgresql" % "postgresql" % "42.6.0",
      "org.reactivemongo" %% "reactivemongo" % "1.0.10",
      "com.dimafeng" %% "testcontainers-scala-mongodb" % testContainersVersion % Test,
      "org.wvlet.airframe" %% "airframe-ulid" % "23.4.7",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.15.0" % Test,
      "com.lihaoyi" %% "fansi" % "0.4.0"
    )
  )
