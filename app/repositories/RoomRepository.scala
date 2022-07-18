package repositories

import akka.actor.ActorRef
import models.game.Game
import repositories.WaitingRoomRepository.{getRoom, rooms}

//todo give participants names?
case class WaitingRoom(participants: Set[ActorRef]){
  def addParticipant(p: ActorRef) = this.copy(participants = participants + p)
  def removeParticipant(p: ActorRef) = this.copy(participants - p)

  def startGame(game: Game): GameRoom = GameRoom(participants, game)
}


case class GameRoom(participants: Set[ActorRef], game: Game) {
  def addParticipant(p: ActorRef) = this.copy(participants = participants + p)

  def handleMsg(msg: String) = this //this is where a turn is taken on the game
}

object WaitingRoom {
  def empty: WaitingRoom = WaitingRoom(Set.empty)
}


object GameRoomRepository {
  private var rooms: Map[String, GameRoom] = Map.empty

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

  def updateRoom(roomId: String, room: GameRoom) = rooms = rooms.updated(roomId, room)

  def addRoom(roomId: String, room: GameRoom) = rooms = rooms + (roomId -> room)

  def addToRoom(roomId: String, af: ActorRef) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(af))

  def getRoom(roomId: String): GameRoom = {
    cleanUp
    rooms.getOrElse(roomId, throw new IllegalArgumentException(s"room: '$roomId' does not exist"))
  }

  def handleMsg(roomId: String, msg: String) = {
    val room = getRoom(roomId).handleMsg(msg)
    updateRoom(roomId, room)
    room
  }
}

object WaitingRoomRepository {

  //room id -> participants
  private var rooms: Map[String, WaitingRoom] = Map.empty.withDefault(_ => WaitingRoom(Set.empty))

  def getRoom(roomId: String): WaitingRoom = {
    cleanUp
    rooms.getOrElse(roomId, WaitingRoom.empty)
  }

  def updateRoom(roomId: String, room: WaitingRoom) = rooms = rooms.updated(roomId, room)

  def migrateToGameRoom(roomId: String, game: Game) = {
    val waitingRoom = getRoom(roomId)
    GameRoomRepository.addRoom(roomId, GameRoom(waitingRoom.participants, game))
    removeRoom(roomId)
  }

  def removeRoom(roomId: String) = {
    rooms = rooms.filter(_._1 != roomId)
  }

  def addToRoom(roomId: String, af: ActorRef) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(af))

  def leaveAllRooms(af: ActorRef) =
    rooms = rooms.map{case (key, room) => key -> room.removeParticipant(af)}

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

}
