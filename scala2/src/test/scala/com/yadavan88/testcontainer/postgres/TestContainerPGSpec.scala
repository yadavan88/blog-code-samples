package com.yadavan88.testcontainer.postgres

import org.scalatest.flatspec.AnyFlatSpec
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.Container
import com.dimafeng.testcontainers.PostgreSQLContainer
import cats.effect.unsafe.implicits.global
import java.time.LocalDate
import java.net.URI
import org.testcontainers.containers
import com.dimafeng.testcontainers.JdbcDatabaseContainer
import com.dimafeng.testcontainers.ForEachTestContainer
import cats.conversions.all

class TestContainerPGSpec extends AnyFlatSpec with ForEachTestContainer {

  override def beforeStop(): Unit = {
    println("Container is about to be stopped")
  }

  override def afterStart(): Unit = {
    println("Container is started")
  }

  val databaseName = "test-database"
  override val container: PostgreSQLContainer =
    PostgreSQLContainer().configure { c =>
      c.withInitScript("init_scripts.sql")
      c.withDatabaseName(databaseName)
    }
  private def getConfig: DBConfig = {
    val jdbcURI = URI.create(container.jdbcUrl.replace("jdbc:", ""))
    val host = jdbcURI.getHost()
    val port = jdbcURI.getPort()

    DBConfig(
      host,
      port,
      container.databaseName,
      container.username,
      Option(container.password)
    )
  }

  it should "get current date from postgresql database" in {

    val dao = new PostgresDAO(getConfig)
    val dateIO = dao.getCurrentDate

    assert(dateIO.unsafeRunSync() == LocalDate.now)

  }

  it should "get rows from movie table" in {

    val dao = new PostgresDAO(getConfig)
    val rows = dao.getMovies.unsafeRunSync()

    assert(rows.size == 2)
    assert(rows.map(_.name) == List("Shawshank Redemptions", "The Prestige"))
    assert(rows.map(_.id).forall(_ > 0))

  }

  it should "save new movie to database and get all movies back" in {

    val dao = new PostgresDAO(getConfig)
    val io = for {
      movies <- dao.getMovies
      _ = assert(movies.size == 2)
      pulpFiction = Movie(5, "Pulp Fiction")
      _ <- dao.saveMovie(pulpFiction)
      allMoviesAgain <- dao.getMovies
      _ = assert(allMoviesAgain.size == 3)
      _ = assert(allMoviesAgain.find(_.id == 5).get.name == "Pulp Fiction")
    } yield ()

    io.unsafeRunSync()

  }

}
