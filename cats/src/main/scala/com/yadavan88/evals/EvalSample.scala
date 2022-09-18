import cats.Eval
object EvalSample extends App {

  val lazyNumber: Eval[Int] = Eval.later {
    println("This is a lazy evaluation")
    100
  }

  println(lazyNumber.value)
  println(lazyNumber.value)

  val eagerNumber: Eval[Int] = Eval.now {
    println("This is an eager number")
    50
  }

  println(eagerNumber)
  println(eagerNumber)

  val alwaysNumber = Eval.always {
    println("This is evaluated each time invoked.")
    0
  }
  println(alwaysNumber.value)
  println(alwaysNumber.value)

  val lazyStr1 = Eval.now {
    println("Evaluating lazy string 1")
    "Cats Eval"
  }
  val lazyStr2 = Eval.now {
    println("Evaluating lazy string 2")
    " is awesome"
  }

  val combined = lazyStr1.flatMap { l1 =>
    println("inside the flatMap")
    lazyStr2.map(l1 + _)
  }
  println(combined)

  def generateEval(num: Int): Eval[String] = {
    println("Inside generateEval")
    Eval.later("Generated-" + num)
  }

  val anotherEval: Eval[String] =
    Eval.later("First value, ").flatMap(f => generateEval(10).map(f + _))
  println(anotherEval)

  def factorialEval(x: BigInt): Eval[BigInt] = if (x == 0) {
    Eval.now(1)
  } else {
    println("Level: " + Thread.currentThread().getStackTrace().length)

    Eval.defer(factorialEval(x - 1)).map(_ * x)
  }
  // factorialEval(100000).value
  // println("calculation done...")

  val longChain: Eval[String] = Eval
    .always { println("We are in Init Step"); "Init Step" }
    .map { s => println("We are in Step 2"); s + ", Step 2" }
    .map { s => println("We are in Step 3"); s + ", Step 3" }
    .map { s => println("We are in Step 4"); s + ", Step 4" }

  println(longChain.value)
  println(longChain.value)
  println(longChain.value)

  val longChainWithMemoize: Eval[String] = Eval
    .always { println("We are in Init Step"); "Init Step" }
    .map { s => println("We are in Step 2"); s + ", Step 2" }
    .map { s => println("We are in Step 3"); s + ", Step 3" }.memoize
    .map { s => println("We are in Step 4"); s + ", Step 4" }

  println(longChainWithMemoize.value)
  println(longChainWithMemoize.value)
  println(longChainWithMemoize.value)

}
