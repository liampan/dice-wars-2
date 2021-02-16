package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import views.html.index

import scala.concurrent.Future

class HomeController @Inject()(view: index,
                               cc: ControllerComponents
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {


  def onPageLoad(): Action[AnyContent] = Action { request =>
    Ok(view()).addingToSession(("user" -> "user1"))(request)
  }


  def socket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      request.session.get("user") match {
        case Some(user) =>
          Right(ActorFlow.actorRef { out =>
            MyWebSocketActor.props(out, user)
          })
        case None => Left(Forbidden)
      }
    )
  }

}
