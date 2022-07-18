package controllers

import akka.actor._
import repositories.WaitingRoom
import repositories.WaitingRoomRepository._

object WaitingRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, out)
    Props(new WaitingRoomSocketActor(out, user, roomId))
  }
}

class WaitingRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg =>
      val room: WaitingRoom = getRoom(roomId)
      room.participants.foreach(_ ! "recivced: " + msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = getRoom(roomId)
    room.participants.foreach(_ ! s"$user has left")
    leaveAllRooms(out)
  }
}
