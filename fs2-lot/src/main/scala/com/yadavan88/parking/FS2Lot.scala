package com.yadavan88.parking

import cats.effect.IO
import fs2.Stream
import scala.concurrent.duration._
import cats.effect.std.Console
import cats.effect.kernel.Ref
import fs2.concurrent.SignallingRef
import cats.syntax.traverse._
import cats.syntax.all._
import scala.util.Random
import fs2.Pipe
import cats.effect.IOApp

object FS2Lot {

  val incomingCarSimulationRate = 10.seconds
  val outgoingCarSimulationRate = 6.seconds
  val outgoingCarStartDelay = 40.second
  val statusStreamRate = 3.seconds

  def init(totalFloors: Int, spotsPerFloor: Int): ParkingLot = {
    val spots = (0 until totalFloors).flatMap { floor =>
      (0 until spotsPerFloor).map(id => ParkingSpot(id, floor, None))
    }
    ParkingLot(spots.toList, totalFloors, spotsPerFloor)
  }

  def incomingCarSimulation(
      pausedRef: SignallingRef[IO, Boolean]
  ): Stream[IO, Car] = {
    Stream
      .awakeEvery[IO](incomingCarSimulationRate)
      .interruptWhen(pausedRef)
      .scan(0) { (counter, _) => counter + 1 }
      .map(counter => Car(counter))
  }

  def outgoingCarSimulation(
      lotRef: Ref[IO, ParkingLot],
      pausedRef: SignallingRef[IO, Boolean]
  ): Stream[IO, Car] = {
    Stream.sleep[IO](10.seconds) *>
      Stream
        .awakeEvery[IO](outgoingCarSimulationRate)
        .interruptWhen(pausedRef)
        .evalMap { _ =>
          for {
            lot <- lotRef.get
            occupiedSpots = lot.spots.filter(_.isOccupied)
            spot <- IO(occupiedSpots(Random.nextInt(occupiedSpots.size)))
          } yield spot.carId.map(id => Car(id, Some(spot.id), Some(spot.floor)))
        }
        .unNone
  }

  def parkCar(lotRef: Ref[IO, ParkingLot]): Pipe[IO, Car, Car] = { cars =>
    cars.evalMap { car =>
      for {
        lot <- lotRef.get
        spot <- IO(lot.findAvailableSpot)
        updatedCar <- spot match {
          case Some(s) =>
            lotRef.update(_.occupySpot(s.id, s.floor, car.id).get) *>
              Console[IO].println(
                s"Car ${car.displayId} parked at ${s.name}"
              ) *>
              IO(car.withSpot(s.id, s.floor))
          case None =>
            Console[IO].println(s"Car ${car.displayId} couldn't find a spot") *>
              IO(car)
        }
      } yield updatedCar
    }
  }

  def removeCar(lotRef: Ref[IO, ParkingLot]): Pipe[IO, Car, Car] = { cars =>
    cars.evalMap { car =>
      for {
        lot <- lotRef.get
        _ <- (car.spotId, car.floor) match {
          case (Some(id), Some(f)) =>
            lotRef.update(_.releaseSpot(id, f).get) *>
              Console[IO].println(
                s"Car ${car.displayId} left from L${f}-${id}"
              )
          case _ =>
            Console[IO].println(
              s"Car ${car.displayId} not found in parking lot"
            )
        }
      } yield car
    }
  }

  def statusStream(
      lotRef: Ref[IO, ParkingLot],
      pausedRef: SignallingRef[IO, Boolean]
  ): Stream[IO, Unit] = {
    Stream
      .awakeEvery[IO](statusStreamRate)
      .interruptWhen(pausedRef)
      .evalMap { _ =>
        for {
          lot <- lotRef.get
          _ <- Console[IO].println("\n=== Parking Lot Status ===")
          _ <- (0 until lot.totalFloors).toList.traverse { floor =>
            val (occupied, total) = lot.getOccupancyByFloor(floor)
            val status =
              if (lot.blockedFloors.contains(floor)) "[CLOSED]" else ""
            val occupancy = s"$occupied/$total"
            val padding = " " * (3 - occupancy.length)
            Console[IO].println(
              s"Floor $floor - $padding$occupancy occupied $status"
            )
          }
          _ <- Console[IO].println("=======================\n")
        } yield ()
      }
  }

  def parseCommand(input: String): Option[Command] = {
    input.trim.toLowerCase match {
      case "pause"           => Some(Command.Pause)
      case "resume"          => Some(Command.Resume)
      case "status"          => Some(Command.Status)
      case "quit"            => Some(Command.Quit)
      case s"block $floor"   => Some(Command.Block(floor.toInt))
      case s"unblock $floor" => Some(Command.Unblock(floor.toInt))
      case _                 => None
    }
  }

  def handlePause(pausedRef: Ref[IO, Boolean]): IO[Unit] = {
    for {
      isPaused <- pausedRef.get
      _ <-
        if (!isPaused) {
          pausedRef.set(true) *>
            Console[IO].println(
              "System paused. Use 'resume' command to continue."
            )
        } else IO.unit
    } yield ()
  }

  def handleResume(pausedRef: Ref[IO, Boolean]): IO[Unit] = {
    for {
      isPaused <- pausedRef.get
      _ <-
        if (isPaused) {
          pausedRef.set(false) *>
            Console[IO].println("System resumed")
        } else IO.unit
    } yield ()
  }

  def handleStatus(lotRef: Ref[IO, ParkingLot]): IO[Unit] = {
    for {
      lot <- lotRef.get
      _ <- (0 until lot.totalFloors).toList.traverse { floor =>
        val (occupied, total) = lot.getOccupancyByFloor(floor)
        val status =
          if (lot.blockedFloors.contains(floor)) "MAINTENANCE"
          else "OPERATIONAL"
        Console[IO].println(
          s"Floor $floor ($status): $occupied/$total spots occupied"
        )
      }
    } yield ()
  }

  def handleBlock(lotRef: Ref[IO, ParkingLot], floor: Int): IO[Unit] = {
    lotRef.update(_.blockFloor(floor)) *>
      Console[IO].println(s"Floor $floor blocked for maintenance")
  }

  def handleUnblock(lotRef: Ref[IO, ParkingLot], floor: Int): IO[Unit] = {
    lotRef.update(_.unblockFloor(floor)) *>
      Console[IO].println(s"Floor $floor unblocked")
  }

  def handleQuit: IO[Unit] = {
    IO.raiseError(new Exception("Quitting program"))
  }

  def handleInvalidCommand: IO[Unit] = {
    Console[IO].println("Invalid command")
  }

  def userInputStream(
      lotRef: Ref[IO, ParkingLot],
      pausedRef: Ref[IO, Boolean]
  ): Stream[IO, Unit] = {
    Stream
      .repeatEval {
        for {
          _ <- Console[IO].println(
            """
              |Available Commands:
              |  pause          - Pause car simulation
              |  resume         - Resume car simulation
              |  status         - Show current parking lot status
              |  block <floor>  - Block a floor for maintenance (e.g., block 2)
              |  unblock <floor>- Unblock a floor (e.g., unblock 2)
              |  quit           - Exit the application
              |Enter command:""".stripMargin
          )
          input <- Console[IO].readLine
          command = parseCommand(input)
          _ <- command match {
            case Some(Command.Pause)          => handlePause(pausedRef)
            case Some(Command.Resume)         => handleResume(pausedRef)
            case Some(Command.Status)         => handleStatus(lotRef)
            case Some(Command.Block(floor))   => handleBlock(lotRef, floor)
            case Some(Command.Unblock(floor)) => handleUnblock(lotRef, floor)
            case Some(Command.Quit)           => handleQuit
            case None                         => handleInvalidCommand
          }
        } yield ()
      }
  }

  def run(totalFloors: Int, spotsPerFloor: Int): IO[Unit] = {
    for {
      lotRef <- Ref[IO].of(init(totalFloors, spotsPerFloor))
      pausedRef <- SignallingRef[IO].of(false)
      _ <- (
        incomingCarSimulation(pausedRef).through(parkCar(lotRef)).void merge
          outgoingCarSimulation(lotRef, pausedRef)
            .through(removeCar(lotRef))
            .void merge
          statusStream(lotRef, pausedRef) merge
          userInputStream(lotRef, pausedRef)
      ).compile.drain
    } yield ()
  }
}

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    FS2Lot.run(5, 5)
  }
}
