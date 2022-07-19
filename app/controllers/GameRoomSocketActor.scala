package controllers

import akka.actor._
import repositories.GameRoom
import repositories.GameRoomRepository.{addToRoom, handleMsg}
import views.html.game.HexView

object GameRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(user, out))
    Props(new GameRoomSocketActor(out, user, roomId))
  }
}

class GameRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg: String =>
      val room: GameRoom = handleMsg(roomId, user, msg)
      room.participants.foreach(_.actor ! HexView(room.game, user).toString)
      if (room.game.thisTurnIsOut) receive("skip-turn")
      if (room.game.isAITurn) receive("ai-turn")
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = handleMsg(roomId, user, s"$user has left")
    room.participants.foreach(_.actor ! HexView(room.game, user).toString)
  }
}
