package controllers

import akka.actor._
import repositories.RoomRepository._

object MyWebSocketActor {
  def props(out: ActorRef, user: String, room: String) = Props(new MyWebSocketActor(out, user, room))
}

class MyWebSocketActor(out: ActorRef, user: String, room: String) extends Actor {

  override def receive = {
    case msg => rooms(room).map(_ ! s"$user: $msg" )
  }

  override def postStop(): Unit = {
    super.postStop()
    leaveAllRooms(out)
  }
}
