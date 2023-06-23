package com.yadavan88.scalatestdemo

import org.scalatest.flatspec.AnyFlatSpec

class CompilationCheckSpec extends AnyFlatSpec {

  object DSL {
    def fancy = "fancy dsl impl"
  }

  it should "check if a code block compiles" in {
    assertCompiles("""DSL.fancy""")
  }

  it should "verify a code block doesnt compile" in {
    assertDoesNotCompile("""DSL.fancyNew""")
  }

}
