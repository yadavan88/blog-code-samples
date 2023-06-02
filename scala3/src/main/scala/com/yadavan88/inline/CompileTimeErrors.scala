package com.yadavan88.inline

import scala.compiletime.*
import ops.string.*

object InlineCompilerError {
  inline def checkVersion(inline versionNo: Int) = {
    inline if (versionNo < 0) {
      error(
        "Invalid version number! Negative versioning is not allowed. " + codeOf(
          versionNo
        )
      )
    } else {
      // nothing to do here
      println(s"Correct version information")
    }
  }

  checkVersion(10)
  // checkVersion(-1)  // Note: Fails compilation
}

object InlineCompilerErrorV2 {
  inline def checkVersion(versionNo: String) = {
    inline if (
      !constValue[Matches[versionNo.type, "[\\d]+\\.[\\d]+[\\.\\d]*"]]
    ) {
      error(
        "Invalid semantic version number format. Value of versionNo provided is " + codeOf(
          versionNo
        )
      )
    } else {
      // nothing to do here
      println(s"Correct version information")
    }
  }
  checkVersion("1.2.3")
  // checkVersion("1.2.x")  // Note: Fails compilation
}
