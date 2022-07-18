package controllers

import akka.actor._
import repositories.GameRoom
import repositories.GameRoomRepository.{addToRoom, handleMsg}
import views.html.game.HexView

object GameRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, out)
    Props(new GameRoomSocketActor(out, user, roomId))
  }
}

class GameRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg =>
      val room: GameRoom = handleMsg(roomId, s"$user: $msg")
      room.participants.foreach(_ ! HexView(room.game).toString)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = handleMsg(roomId, s"$user has left")
    room.participants.foreach(_ ! HexView(room.game).toString)
  }
}
