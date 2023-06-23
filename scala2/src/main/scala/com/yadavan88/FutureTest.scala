package com.yadavan88

import scala.concurrent.Future
import scala.util.Random

object FutureTest extends App {

  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val range = (1 to 100).toList

  val res = range.map(futureGen)
  
  def futureGen(id: Int) = {
    Future {
      Random.nextInt(50)
      Thread.sleep(50 + Random.nextInt(50))
      println("querying for "+id)
    }
  }
  
}
