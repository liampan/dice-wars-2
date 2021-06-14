package controllers

import akka.actor._
import repositories.RoomRepository._
import views.html.messenger

object MyWebSocketActor {
  def props(out: ActorRef, user: String, room: String): Props = {
    addToRoom(room, out)
    Props(new MyWebSocketActor(out, user, room))
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
    getRoom(roomId).participants.foreach(_ ! s"$user has left")
    leaveAllRooms(out)
  }
}
