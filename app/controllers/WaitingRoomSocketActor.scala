package controllers

import akka.actor._
import repositories.WaitingRoom
import repositories.WaitingRoomRepository._

case class PlayerActor(userId: String, actor: ActorRef)

object WaitingRoomSocketActor {
  def props(out: ActorRef, user: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(user, out))
    Props(new WaitingRoomSocketActor(out, user, roomId))
  }
}

class WaitingRoomSocketActor(out: ActorRef, user: String, roomId: String) extends Actor {

  override def receive = {
    case msg: String => getRoom(roomId).handleMsg(user, msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = getRoom(roomId)
    room.participants.foreach(_.actor ! s"$user has left")
    leaveAllRooms(out)
  }
}
