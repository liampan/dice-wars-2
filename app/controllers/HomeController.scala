package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import views.html.index

import scala.concurrent.Future

class HomeController @Inject()(view: index,
                               cc: ControllerComponents,
                               userAction: UserAction
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def enterRoom(room: String): Action[AnyContent] = userAction {
    request =>
      Ok(view())
        .addingToSession("username" -> request.userName, "roomname" -> room)(request)
  }

  def socket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      (request.session.get("username"), request.session.get("roomname")) match {
        case (Some(user), Some(room)) =>
          Right(ActorFlow.actorRef { out =>
            MyWebSocketActor.props(out, user, room)
          })
        case _ => Left(Forbidden)
      }
    )
  }

}
