// This is an independent module, not part of the main build.sbt.
// As a result, you can open this module directly in the IDE
name := "small-app"
scalaVersion := "3.3.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "os-lib" % "0.10.2"
)

//sbt-assembly related settings
assembly / mainClass := Some(
  "com.yadavan88.app.mainMethod" // since @main method name will be the class name of the main class in scala 3
)
assembly / assemblyJarName := "smallApp.jar"

//sbt-native-packager related settings
enablePlugins(JavaAppPackaging)
maintainer := "Yadukrishnan <yadavan88@gmail.com>"
Compile / mainClass := Some("com.yadavan88.app.mainMethod")

//sbt-native-packager's jlink related settings
enablePlugins(JlinkPlugin)

jlinkIgnoreMissingDependency := JlinkIgnore.only(
  "scala.quoted" -> "scala",
  "scala.quoted.runtime" -> "scala"
)

//sbt proguard settings
enablePlugins(SbtProguard)
Proguard / proguardOptions ++= Seq(
  "-dontoptimize",
  "-dontnote",
  "-dontwarn",
  "-ignorewarnings"
)
Proguard / proguardOptions += ProguardOptions.keepMain(
  "com.yadavan88.app.mainMethod"
)
Proguard / proguardInputs := (Compile / dependencyClasspath).value.files
Proguard / proguardFilteredInputs ++= ProguardOptions.noFilter(
  (Compile / packageBin).value
)
Proguard / assemblyJarName := "small-app-proguard.jar"
