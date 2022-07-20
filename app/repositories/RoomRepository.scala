package repositories

import akka.actor.ActorRef
import controllers.PlayerActor
import models.game.Game
import repositories.GameRoomRepository.{getRoom, updateRoom}
import views.html.game.HexView
import views.html.messenger

import scala.util.matching.Regex

//todo give participants names?
case class WaitingRoom(roomId: String, participants: Set[PlayerActor], log: Seq[String] = Seq.empty){
  def addParticipant(p: PlayerActor) = this.copy(participants = participants + p)
  def removeParticipant(p: ActorRef) = this.copy(participants = participants.filterNot(_.actor == p))
  def addLog(msg: String) = this.copy(log = log :+ msg)

  def handleMsg(user: String, msg: String) = msg match {
    case _ => execute(_.addLog(user + ": " + msg))
  }

  private def execute(action: WaitingRoom => WaitingRoom): Unit = {
    val r = WaitingRoomRepository.updateRoom(roomId, action(this))
    r.participants.foreach(_.actor ! messenger(r.log).toString())
  }
}


case class GameRoom(roomId: String, participants: Set[PlayerActor], game: Game) {
  def addParticipant(p: PlayerActor) = this.copy(participants = participants + p)

  //create a commands object that creates regex
  private val ClickMine: Regex = "click-mine-(.*)".r
  private val ClickTheirs: Regex = "click-theirs-(.*)".r

  def handleMsg(user: String, msg: String): Unit = msg match {
    case ClickMine(id) => execute(_.rightPlayer(user))(_.clickMine(id))
    case ClickTheirs(id) => execute(_.rightPlayer(user))(_.attack(id))
    case "end-turn" => execute(_.rightPlayer(user))(_.endTurn)
    case "ai-turn" => execute(_.isAITurn)(_.playThisAITurn)
    case "skip-turn" => execute(_.thisTurnIsOut)(_.skipTurn)
    case _ => doNothing()
  }

  private def doNothing(): Unit = execute(_ => false)(identity)

  private def execute(predicate: Game => Boolean)(action: Game => Game): Unit = {
    val gr = if (predicate(game)) this.copy(game = action(game)) else this
    updateRoom(roomId, gr)
    gr.participants.foreach(_.actor ! "get-board")
  }
}

object GameRoomRepository {
  private var rooms: Map[String, GameRoom] = Map.empty

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

  def updateRoom(roomId: String, room: GameRoom) = rooms = rooms.updated(roomId, room)

  def addRoom(room: GameRoom) = {
    rooms = rooms + (room.roomId -> room)
    room
  }

  def addToRoom(roomId: String, player: PlayerActor) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(player))

  def getRoom(roomId: String): GameRoom = {
    cleanUp
    rooms.getOrElse(roomId, throw new IllegalArgumentException(s"room: '$roomId' does not exist"))
  }

  def handleMsg(roomId: String, user: String, msg: String) =
    getRoom(roomId).handleMsg(user, msg)
}

object WaitingRoomRepository {

  //room id -> participants
  private var rooms: Map[String, WaitingRoom] = Map.empty

  def getRoom(roomId: String): WaitingRoom = {
    cleanUp
    rooms.getOrElse(roomId, WaitingRoom(roomId, Set.empty))
  }

  def updateRoom(roomId: String, room: WaitingRoom) = {
    rooms = rooms.updated(roomId, room)
    room
  }

  def migrateToGameRoom(roomId: String, game: Game) = {
    val waitingRoom = getRoom(roomId)
    val newRoom = GameRoomRepository.addRoom(GameRoom(roomId, waitingRoom.participants, game))
    removeRoom(roomId)
    newRoom
  }

  def removeRoom(roomId: String) = {
    rooms = rooms.filter(_._1 != roomId)
  }

  def addToRoom(roomId: String, player: PlayerActor) =
    rooms = rooms.updated(roomId, getRoom(roomId).addParticipant(player))

  def leaveAllRooms(af: ActorRef) =
    rooms = rooms.map{case (key, room) => key -> room.removeParticipant(af)}

  def cleanUp =
    rooms = rooms.filter{case (_, room) => room.participants.nonEmpty }

}
