package com.yadavan88.parking

import scala.util.Random

sealed trait Command
object Command {
  case object Pause extends Command
  case object Resume extends Command
  case object Status extends Command
  case object Quit extends Command
  case class Block(floor: Int) extends Command
  case class Unblock(floor: Int) extends Command
}
case class Car(id: Int, spotId: Option[Int] = None, floor: Option[Int] = None) {
  def withSpot(spotId: Int, floor: Int): Car = copy(spotId = Some(spotId), floor = Some(floor))
  def displayId: String = s"CAR-${String.format("%03d", id)}"
}
case class ParkingSpot(id: Int, floor: Int, carId: Option[Int] = None) {
  def name = s"L${floor}-${id}"
  def isOccupied: Boolean = carId.isDefined
}
case class ParkingLot(
    spots: List[ParkingSpot],
    totalFloors: Int,
    spotsPerFloor: Int,
    blockedFloors: Set[Int] = Set.empty
) {
  def findAvailableSpot: Option[ParkingSpot] = {
    (0 until totalFloors)
      .filterNot(blockedFloors.contains) 
      .flatMap(findAvailableSpotOnFloor)
      .headOption 
  }

  def findAvailableSpotOnFloor(floor: Int): Option[ParkingSpot] =
    spots.find(spot =>
      spot.floor == floor && !spot.isOccupied && !blockedFloors.contains(floor)
    )

  def occupySpot(spotId: Int, floorId: Int, carId: Int): Option[ParkingLot] = {
    val spot = spots.find(s => s.id == spotId && s.floor == floorId)
    spot match {
      case Some(s) =>
        val updatedSpots = spots.map { spot =>
          if (spot.id == spotId && spot.floor == floorId) spot.copy(carId = Some(carId))
          else spot
        }
        Some(copy(spots = updatedSpots))
      case None => None
    }
  }

  def releaseSpot(spotId: Int, floorId: Int): Option[ParkingLot] = {
    val spot = spots.find(s => s.id == spotId && s.floor == floorId)
    spot match {
      case Some(s) =>
        val updatedSpots = spots.map { spot =>
          if (spot.id == spotId && spot.floor == floorId) spot.copy(carId = None)
          else spot
        }
        Some(copy(spots = updatedSpots))
      case None => None
    }
  }

  def getOccupancyByFloor(floor: Int): (Int, Int) = {
    val floorSpots = spots.filter(_.floor == floor)
    val occupied = floorSpots.count(_.carId.isDefined)
    (occupied, floorSpots.size)
  }

  def blockFloor(floor: Int): ParkingLot =
    copy(blockedFloors = blockedFloors + floor)

  def unblockFloor(floor: Int): ParkingLot =
    copy(blockedFloors = blockedFloors - floor)
}
