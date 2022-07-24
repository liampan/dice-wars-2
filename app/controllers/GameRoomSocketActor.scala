package controllers

import akka.actor._
import repositories.GameRoom
import repositories.GameRoomRepository.{addToRoom, getRoom, handleMsg, leaveAllRooms}
import views.html.game.HexPartial

object GameRoomSocketActor {
  def props(out: ActorRef, userId: String, userName: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(userId, userName, out))
    Props(new GameRoomSocketActor(out, userId, userName, roomId))
  }
}

class GameRoomSocketActor(out: ActorRef, userId: String, userName: String, roomId: String) extends Actor {

  override def receive: Receive = {
    case "get-board" =>
      val room: GameRoom = getRoom(roomId)
      room.participants.foreach(player => player.actor ! HexPartial(room.game, player.userId, room.game.gameComplete).toString)
      if (room.game.humanPlayersLeft) {
        if (room.game.thisTurnIsOut) receive("skip-turn")
        else if (room.game.isAITurn) receive("ai-turn")
      }
    case msg: String => handleMsg(roomId, userId, msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    leaveAllRooms(out)
    getRoom(roomId).participants.foreach(_.actor ! "get-board")
  }
}
