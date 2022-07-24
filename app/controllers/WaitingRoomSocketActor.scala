package controllers

import akka.actor._
import repositories.WaitingRoomRepository._

case class PlayerActor(userId: String, userName: String, actor: ActorRef)

object WaitingRoomSocketActor {
  def props(out: ActorRef, userId: String, userName: String, roomId: String): Props = {
    addToRoom(roomId, PlayerActor(userId, userName, out))
    Props(new WaitingRoomSocketActor(out, userId, userName, roomId))
  }
}

class WaitingRoomSocketActor(out: ActorRef, userId: String, userName: String, roomId: String) extends Actor {

  override def receive = {
    case msg: String => getRoom(roomId).handleMsg(userId, userName, msg)
  }

  override def postStop(): Unit = {
    super.postStop()
    val room = getRoom(roomId)
    room.participants.foreach(_.actor ! s"$userName has left")
    leaveAllRooms(out)
  }
}
