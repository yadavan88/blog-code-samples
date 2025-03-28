package com.yadavan88.parking

import cats.effect.IO
import fs2.Stream
import scala.concurrent.duration._
import cats.effect.std.Console
import cats.effect.kernel.Ref
import cats.syntax.traverse._
import cats.syntax.all._
import scala.util.Random
import fs2.Pipe


case class Car(id: String, spotId: Option[Int] = None) {
  def withSpot(spotId: Int): Car = copy(spotId = Some(spotId))
}
case class ParkingSpot(id: Int, floor: Int, isOccupied: Boolean) {
  def name = s"L${floor}-${id}"
}
case class ParkingLot(
    spots: List[ParkingSpot],
    totalFloors: Int,
    spotsPerFloor: Int,
    blockedFloors: Set[Int] = Set.empty
) {
  def findAvailableSpot: Option[ParkingSpot] =
    spots.find(spot => !spot.isOccupied && !blockedFloors.contains(spot.floor))

  def findAvailableSpotOnFloor(floor: Int): Option[ParkingSpot] =
    spots.find(spot => spot.floor == floor && !spot.isOccupied && !blockedFloors.contains(floor))

  def occupySpot(spotId: Int): Option[ParkingLot] = {
    val updatedSpots = spots.map { spot =>
      if (spot.id == spotId) spot.copy(isOccupied = true)
      else spot
    }
    Some(copy(spots = updatedSpots))
  }

  def releaseSpot(spotId: Int): Option[ParkingLot] = {
    val updatedSpots = spots.map { spot =>
      if (spot.id == spotId) spot.copy(isOccupied = false)
      else spot
    }
    Some(copy(spots = updatedSpots))
  }

  def getOccupancyByFloor(floor: Int): (Int, Int) = {
    val floorSpots = spots.filter(_.floor == floor)
    (floorSpots.count(_.isOccupied), floorSpots.size)
  }

  def blockFloor(floor: Int): ParkingLot = 
    copy(blockedFloors = blockedFloors + floor)

  def unblockFloor(floor: Int): ParkingLot = 
    copy(blockedFloors = blockedFloors - floor)
}

object FS2Lot {

  def init(totalFloors: Int, spotsPerFloor: Int): ParkingLot = {
    val spots = (0 until totalFloors).flatMap { floor =>
      (0 until spotsPerFloor).map(id => ParkingSpot(id, floor, false))
    }
    ParkingLot(spots.toList, totalFloors, spotsPerFloor)
  }

  // Simulation streams that generate cars at intervals
  def incomingCarSimulation: Stream[IO, Car] = {
    Stream
      .awakeEvery[IO](5.seconds)
      .map(_ => Car(s"CAR-${Random.nextInt(1000)}"))
  }

  def outgoingCarSimulation(lotRef: Ref[IO, ParkingLot]): Stream[IO, Car] = {
    Stream
      .awakeEvery[IO](10.seconds)
      .evalMap { _ =>
        for {
          lot <- lotRef.get
          occupiedSpots = lot.spots.filter(_.isOccupied)
          spot <- IO(occupiedSpots.headOption)
        } yield spot.map(s => Car(s"CAR-${s.id}", Some(s.id)))
      }
      .unNone
  }

  // Core parking lot logic as pipes
  def parkCar(lotRef: Ref[IO, ParkingLot]): Pipe[IO, Car, Car] = { cars =>
    cars.evalMap { car =>
      for {
        lot <- lotRef.get
        spot <- IO(lot.findAvailableSpot)
        _ <- spot match {
          case Some(s) => 
            lotRef.update(_.occupySpot(s.id).get) *>
            Console[IO].println(s"Car ${car.id} parked at ${s.name}")
          case None => 
            Console[IO].println(s"Car ${car.id} couldn't find a spot")
        }
      } yield car
    }
  }

  def removeCar(lotRef: Ref[IO, ParkingLot]): Pipe[IO, Car, Car] = { cars =>
    cars.evalMap { car =>
      for {
        lot <- lotRef.get
        spotId <- IO(car.spotId)
        _ <- spotId match {
          case Some(id) => 
            lotRef.update(_.releaseSpot(id).get) *>
            Console[IO].println(s"Car ${car.id} left from ${lot.spots.find(_.id == id).map(_.name).getOrElse("unknown")}")
          case None => 
            Console[IO].println(s"Car ${car.id} not found in parking lot")
        }
      } yield car
    }
  }

  def statusStream(lotRef: Ref[IO, ParkingLot]): Stream[IO, Unit] = {
    Stream
      .awakeEvery[IO](3.seconds)
      .evalMap { _ =>
        for {
          lot <- lotRef.get
          _ <- (0 until lot.totalFloors).toList.traverse { floor =>
            val (occupied, total) = lot.getOccupancyByFloor(floor)
            val status = if (lot.blockedFloors.contains(floor)) "MAINTENANCE" else "OPERATIONAL"
            Console[IO].println(s"Floor $floor ($status): $occupied/$total spots occupied")
          }
        } yield ()
      }
  }

  def parseCommand(input: String): Option[Command] = {
    input.trim.toLowerCase match {
      case "pause" => Some(Command.Pause)
      case "status" => Some(Command.Status)
      case "quit" => Some(Command.Quit)
      case s"block $floor" => Some(Command.Block(floor.toInt))
      case s"unblock $floor" => Some(Command.Unblock(floor.toInt))
      case _ => None
    }
  }

  sealed trait Command
  object Command {
    case object Pause extends Command
    case object Status extends Command
    case object Quit extends Command
    case class Block(floor: Int) extends Command
    case class Unblock(floor: Int) extends Command
  }

  def userInputStream(lotRef: Ref[IO, ParkingLot]): Stream[IO, Unit] = {
    Stream
      .repeatEval {
        for {
          _ <- Console[IO].println("\nEnter command (pause/status/block <floor>/unblock <floor>/quit): ")
          input <- Console[IO].readLine
          command = parseCommand(input)
          _ <- command match {
            case Some(Command.Pause) => 
              Console[IO].println("System paused. Press Enter to continue...") *> Console[IO].readLine
            case Some(Command.Status) => 
              for {
                lot <- lotRef.get
                _ <- (0 until lot.totalFloors).toList.traverse { floor =>
                  val (occupied, total) = lot.getOccupancyByFloor(floor)
                  val status = if (lot.blockedFloors.contains(floor)) "MAINTENANCE" else "OPERATIONAL"
                  Console[IO].println(s"Floor $floor ($status): $occupied/$total spots occupied")
                }
              } yield ()
            case Some(Command.Block(floor)) => 
              lotRef.update(_.blockFloor(floor)) *>
              Console[IO].println(s"Floor $floor blocked for maintenance")
            case Some(Command.Unblock(floor)) => 
              lotRef.update(_.unblockFloor(floor)) *>
              Console[IO].println(s"Floor $floor unblocked")
            case Some(Command.Quit) => IO.raiseError(new Exception("Quitting program"))
            case None => Console[IO].println(s"Invalid command: $input")
          }
        } yield ()
      }
  }

  def run(totalFloors: Int, spotsPerFloor: Int): IO[Unit] = {
    for {
      lotRef <- Ref[IO].of(init(totalFloors, spotsPerFloor))
      _ <- (
        incomingCarSimulation.through(parkCar(lotRef)).void merge
        outgoingCarSimulation(lotRef).through(removeCar(lotRef)).void merge
        statusStream(lotRef) merge
        userInputStream(lotRef)
      ).compile.drain
    } yield ()
  }
}
