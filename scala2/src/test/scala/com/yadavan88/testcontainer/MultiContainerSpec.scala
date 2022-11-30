package com.yadavan88.testcontainer

import org.scalatest.flatspec.AnyFlatSpec
import com.dimafeng.testcontainers.ForEachTestContainer
import com.dimafeng.testcontainers.Container
import com.yadavan88.testcontainer.mongo._
import com.dimafeng.testcontainers.MongoDBContainer
import com.yadavan88.testcontainer.postgres.Movie
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.flatspec.AsyncFlatSpec
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.MultipleContainers
import com.yadavan88.testcontainer.postgres.PostgresDAO
import com.yadavan88.testcontainer.postgres.DBConfig
import java.net.URI

class MultiContainerSpec extends AsyncFlatSpec with ForEachTestContainer {

  val mongoDBContainer: MongoDBContainer = new MongoDBContainer()
  val postgresContainer = PostgreSQLContainer().configure { c =>
    c.withInitScript("init_scripts.sql")
  }
  override val container =
    MultipleContainers(postgresContainer, mongoDBContainer)

  private def getPostgresConfig: DBConfig = {
    val jdbcURI = URI.create(postgresContainer.jdbcUrl.replace("jdbc:", ""))
    val host = jdbcURI.getHost()
    val port = jdbcURI.getPort()

    DBConfig(
      host,
      port,
      postgresContainer.databaseName,
      postgresContainer.username,
      Option(postgresContainer.password)
    )
  }

  it should "save movie to mongo and get the result back" in {
    val config =
      MongoConfig(mongoDBContainer.container.getConnectionString(), "movies")
    val mongoDao = new MongoDao(config)
    val movie = Movie(99, "Fight Club")
    for {
      _ <- mongoDao.saveMovie(movie)
      all <- mongoDao.getMovies()
    } yield {
      assert(all.size == 1)
    }

  }

  it should "get movies from postgres and insert into mongo" in {
    val config =
      MongoConfig(mongoDBContainer.container.getConnectionString(), "movies")
    val mongoDao = new MongoDao(config)
    val postgreDao = new PostgresDAO(getPostgresConfig)
    import cats.effect.unsafe.implicits.global

    for {
      pgMovies <- postgreDao.getMovies.unsafeToFuture()
      _ <- mongoDao.saveMovies(pgMovies)
      all <- mongoDao.getMovies()
    } yield {
      assert(all.size == 2)
      assert(all.map(_.name) == Seq("Shawshank Redemptions", "The Prestige"))
    }

  }

}
