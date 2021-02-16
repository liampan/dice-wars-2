package controllers

import akka.actor._

object MyWebSocketActor {
  def props(out: ActorRef, user: String) = Props(new MyWebSocketActor(out, user))
}

class MyWebSocketActor(out: ActorRef, user: String) extends Actor {
  def receive = {
    case "hello" => out ! s"hello $user"
    case msg: String => out ! ("I received your message: " + msg)
  }
}
