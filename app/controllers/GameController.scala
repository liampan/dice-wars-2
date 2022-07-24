package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey, usernameKey}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Range => _, _}
import views.html.game.GameScreenView

import scala.concurrent.Future

class GameController @Inject()(gameView: GameScreenView,
                               cc: ControllerComponents,
                               userAction: UserAction,
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def game() = userAction {
    Ok(gameView())
  }

  def socketWaitingRoom: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      (request.session.get(userIdKey), request.session.get(usernameKey), request.session.get(roomKey)) match {
        case (Some(userId), Some(userName), Some(room)) =>
          Right(ActorFlow.actorRef { out =>
            WaitingRoomSocketActor.props(out, userId, userName, room)
          })
        case _ => Left(Forbidden)
      }
    )
  }

  def socketGameRoom: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      (request.session.get(userIdKey), request.session.get(usernameKey), request.session.get(roomKey)) match {
        case (Some(userId), Some(userName), Some(room)) =>
          Right(ActorFlow.actorRef { out =>
            GameRoomSocketActor.props(out, userId, userName, room)
          })
        case _ => Left(Forbidden)
      }
    )
  }

}

object GameController {
  val roomKey = "room-name"
  val userIdKey = "user-id"
  val usernameKey = "user-name"
}
