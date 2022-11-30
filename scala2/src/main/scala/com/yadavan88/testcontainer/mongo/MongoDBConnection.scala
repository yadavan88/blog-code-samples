package com.yadavan88.testcontainer.mongo

import reactivemongo.api.AsyncDriver
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.BSONDocumentWriter
import reactivemongo.api.bson.BSONDocumentReader
import reactivemongo.api.bson.BSONDocument
import com.yadavan88.testcontainer.postgres.Movie

object MovieMacros {
  implicit def movieWriter: BSONDocumentWriter[Movie] = Macros.writer[Movie]
  implicit def movieReader: BSONDocumentReader[Movie] = Macros.reader[Movie]
}

case class MongoConfig(url: String, dbName: String)

object MovieConnection {
  val driver = AsyncDriver()
  def getDB(config: MongoConfig): Future[DB] = {
    for {
      parsedUri <- MongoConnection.fromString(config.url)
      con <- driver.connect(parsedUri)
      db <- con.database(config.dbName)
    } yield db
  }
}

class MongoDao(config: MongoConfig) {

  import MovieMacros._
  val collectionName = "movies"

  def getCollection = {
    for {
      db <- MovieConnection.getDB(config)
      col = db.collection(collectionName)
    } yield col
  }

  def getMovies(): Future[List[Movie]] = {
    for {
      collection <- getCollection
      movies <- collection.find(BSONDocument()).cursor[Movie]().collect[List]()
    } yield movies
  }

  def saveMovie(movie: Movie): Future[Unit] = {
    for {
      collection <- getCollection
      _ <- collection.insert.one(movie)
    } yield ()
  }

  def saveMovies(movies: List[Movie]): Future[Unit] = {
    for {
      collection <- getCollection
      _ <- collection.insert.many(movies)
    } yield ()

  }
}
