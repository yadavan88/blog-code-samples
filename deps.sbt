import sbt.*
import Keys.*

import scala.collection.mutable.ListBuffer

lazy val generateDeps =
  taskKey[Unit]("Generates direct library dependencies of the project")

generateDeps := Def.taskDyn {

  def formatAsMarkdownTable(
      artifacts: Seq[(String, String, String, Option[String])]
  ): String = {
    val header = "| Group ID | Artifact ID | Version |\n| --- | --- | --- |"
    val rows = artifacts
      .groupBy { case (groupId, artId, version, _) =>
        s"| $groupId | $artId | $version |"
      }
      .keys
      .toSeq
      .sorted
    (header +: rows).mkString("\n")
  }

  val projectRefs: Seq[ProjectRef] = loadedBuild.value.allProjectRefs.map(_._1)
  val dependencies: ListBuffer[(String, String, String, Option[String])] =
    ListBuffer.empty
  Def
    .sequential {
      projectRefs
        .map { projectRef =>
          Def.task {
            val allDeps =
              (projectRef / libraryDependencies).value.sortBy(_.organization)
            allDeps.map { dep =>
              val configurations = dep.configurations
              val res =
                (dep.organization, dep.name, dep.revision, configurations)
              dependencies.append(res)
              res
            }
          }
        }
    }
    .map { _ =>
      val mainDeps = dependencies.filter(_._4.isEmpty).sortBy(_._1).distinct
      val testDeps = dependencies.filter(_._4.nonEmpty).sortBy(_._1).distinct
      val mainDepsTable = formatAsMarkdownTable(mainDeps)
      val testDepsTable = formatAsMarkdownTable(testDeps)
      val markdownContent = s"""# List of direct dependencies
                             |
                             |This document lists all direct dependencies of the project.
                             |
                             |## Main Libraries
                             |
                             |$mainDepsTable
                             |
                             |## Test Libraries
                             |
                             |$testDepsTable
                             |
                             |""".stripMargin

      val file = new File("directDependencies.md")
      IO.write(file, markdownContent)
      ()
    }
}.value
