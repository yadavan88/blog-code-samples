object EmbeddedMongoInstance {
  def start(port: Int) = {
    println(fansi.Color.Green("Starting the MongoDB Instance on port: "+port))
  }

  def stop(port: Int) = {
    println(fansi.Color.Yellow("Stopping the MongoDB Instance on port: " + port))
  }
}