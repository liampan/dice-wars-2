package controllers

import akka.actor._
import repositories.GameRoom
import repositories.GameRoomRepository.{addToRoom, getRoom, handleMsg}
import views.html.game.HexView

object GameRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(user, out))
    Props(new GameRoomSocketActor(out, user, roomId))
  }
}

class GameRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case "get-board" =>
      val room: GameRoom = getRoom(roomId)
      room.participants.foreach(_.actor ! HexView(room.game, user, room.game.gameComplete).toString)
    case msg: String =>
      val room: GameRoom = handleMsg(roomId, user, msg)
      room.participants.foreach(_.actor ! "get-board")
      if (room.game.humanPlayersLeft) {
        if (room.game.thisTurnIsOut) receive("skip-turn")
        if (room.game.isAITurn) receive("ai-turn")
      }
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = handleMsg(roomId, user, s"$user has left")
    room.participants.foreach(_.actor ! "get-board")
  }
}
