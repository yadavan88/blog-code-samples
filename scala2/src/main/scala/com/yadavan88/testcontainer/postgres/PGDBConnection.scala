package com.yadavan88.testcontainer.postgres

import cats.effect._
import skunk._
import skunk.implicits._
import skunk.codec.all._
import natchez.Trace.Implicits.noop
import skunk.data.Completion

case class DBConfig(
    host: String,
    port: Int,
    db: String,
    username: String,
    pwd: Option[String]
)

object PGDBConnection {
  def getSession(config: DBConfig): Resource[IO, Session[IO]] = {
    println(s"Connecting to database `${config.db}` at port ${config.port} ")
    Session.single(
      host = config.host,
      port = config.port,
      database = config.db,
      user = config.username,
      password = config.pwd
    )
  }
}

case class Movie(id: Long, name: String)

class PostgresDAO(config: DBConfig) {

  def getCurrentDate = {
    PGDBConnection.getSession(config).use { session =>
      for {
        date <- session.unique(sql"select current_date".query(date))
        _ <- IO.println("Current date is " + date)
      } yield date
    }
  }
  val movies: Decoder[Movie] =
    (int8 ~ varchar).map { case (n, p) => Movie(n, p) }

  def getMovies = {
    PGDBConnection.getSession(config).use { session =>
      for {
        rows <- session.execute(sql"select * from public.movie".query(movies))
      } yield rows
    }
  }

  def saveMovie(movieRow: Movie): IO[Unit] = {
    PGDBConnection.getSession(config).use { session =>
      for {
        cmd <- session
          .prepare(
            sql"insert into public.movie values($int8, $varchar)".command
              .to[Movie]
          )
        insert <- cmd.execute(movieRow)
      } yield insert
    }
  }.void

}

object TestApp extends IOApp.Simple {
  val config = DBConfig("localhost", 5432, "testcontainer", "admin", None)
  def run: IO[Unit] = PGDBConnection.getSession(config).use { session =>
    for {
      date <- session.unique(sql"select current_date".query(date))
      _ <- IO.println("Current date is " + date)
    } yield ()
  }
}
