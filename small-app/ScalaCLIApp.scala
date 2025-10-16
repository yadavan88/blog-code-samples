//> using scala "3.3.7"
//> using dep com.lihaoyi::os-lib:0.9.1
package com.yadavan88.scalacli
import os._
// use the following command to create packaged app
// scala-cli --power package ScalaCLIApp.scala -o smallApp
object ScalaCliApp {
  @main def app() = {
    val path = os.pwd.toString
    println(s"""
               | Hello from the scala-cli packaged app!
               | Current Path: ${path}
                """.stripMargin)
  }
}
