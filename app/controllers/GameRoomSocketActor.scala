package controllers

import akka.actor._
import repositories.GameRoom
import repositories.GameRoomRepository.{addToRoom, getRoom, handleMsg, leaveAllRooms}
import views.html.game.HexView

object GameRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(user, out))
    Props(new GameRoomSocketActor(out, user, roomId))
  }
}

class GameRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive: Receive = {
    case "get-board" =>
      val room: GameRoom = getRoom(roomId)
      room.participants.foreach(player => player.actor ! HexView(room.game, player.userId, room.game.gameComplete).toString)
      if (room.game.humanPlayersLeft) {
        if (room.game.thisTurnIsOut) receive("skip-turn")
        else if (room.game.isAITurn) receive("ai-turn")
      }
    case msg: String => handleMsg(roomId, user, msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    leaveAllRooms(out)
    getRoom(roomId).participants.foreach(_.actor ! "get-board")
  }
}
