package com.yadavan88.scalatestdemo

import com.yadavan88.tags.SpecialTest
import org.scalatest.flatspec.AnyFlatSpec

//run using testOnly *TaggingAnnotationSpec -- -n "com.yadavan88.tags.SpecialTest"
@SpecialTest
class TaggingAnnotationSpec extends AnyFlatSpec {

  it should "pass this test" in {
    succeed
  }
  it should "pass this test as well" in {
    succeed
  }
  it should "again pass" in {
    succeed
  }
}
