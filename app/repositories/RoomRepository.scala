package repositories

import akka.actor.ActorRef
import controllers.Player
import models.game.Game

import scala.util.matching.Regex

//todo give participants names?
case class WaitingRoom(participants: Set[Player]){
  def addParticipant(p: Player) = this.copy(participants = participants + p)
  def removeParticipant(p: ActorRef) = this.copy(participants.filterNot(_.actor == p))

  def startGame(game: Game): GameRoom = GameRoom(participants, game)
}


case class GameRoom(participants: Set[Player], game: Game) {
  def addParticipant(p: Player) = this.copy(participants = participants + p)

  //create a commands object that creates regex
  private val ClickMine: Regex = "click-mine-(.*)".r
  private val ClickTheirs: Regex = "click-theirs-(.*)".r

  def handleMsg(user: String, msg: String): GameRoom = msg match {
    case ClickMine(id) => check(_.rightPlayer(user))(_.clickMine(user, id))
    case ClickTheirs(id) => check(_.rightPlayer(user))(_.attack(user, id))
    case "end-turn" => this.copy(game = game.endTurn(user))
    case "ai-turn" => check(_.isAITurn)(_.playThisAITurn)
    case "skip-turn" => check(_.thisTurnIsOut)(_.skipTurn)
    case _ => this
  }

  private def check(f: Game => Boolean)(g: Game => Game) =
    if (f(game)) this.copy(game = g(game)) else this
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

  def addToRoom(roomId: String, player: Player) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(player))

  def getRoom(roomId: String): GameRoom = {
    cleanUp
    rooms.getOrElse(roomId, throw new IllegalArgumentException(s"room: '$roomId' does not exist"))
  }

  def handleMsg(roomId: String, user: String, msg: String) = {
    val room = getRoom(roomId).handleMsg(user, msg)
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

  def addToRoom(roomId: String, player: Player) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(player))

  def leaveAllRooms(af: ActorRef) =
    rooms = rooms.map{case (key, room) => key -> room.removeParticipant(af)}

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

}
