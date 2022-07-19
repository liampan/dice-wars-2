package controllers

import akka.actor._
import repositories.WaitingRoom
import repositories.WaitingRoomRepository._

case class Player(userId: String, actor: ActorRef)

object WaitingRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, Player(user, out))
    Props(new WaitingRoomSocketActor(out, user, roomId))
  }
}

class WaitingRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg =>
      val room: WaitingRoom = getRoom(roomId)
      room.participants.foreach(_.actor ! "recivced: " + msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = getRoom(roomId)
    room.participants.foreach(_.actor ! s"$user has left")
    leaveAllRooms(out)
  }
}
