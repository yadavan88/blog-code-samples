package com.yadavan88.app

@main
def mainMethod() = {
  val path = os.pwd.toString
  println(s"""
       | Hello from the packaged app!
       | Current Path: ${path}
      """.stripMargin)
}
