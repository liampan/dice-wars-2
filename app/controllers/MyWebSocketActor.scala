package controllers

import akka.actor._
import repositories.RoomRepository._
import views.html.messenger

object MyWebSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, out)
    Props(new MyWebSocketActor(out, user, roomId))
  }
}

class MyWebSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg =>
      val room = addMsg(roomId, s"$user: $msg")
      room.participants.map(_ ! messenger(room.messages).toString)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = addMsg(roomId, s"$user has left")
    room.participants.foreach(_ ! messenger(room.messages).toString)
    leaveAllRooms(out)
  }
}
