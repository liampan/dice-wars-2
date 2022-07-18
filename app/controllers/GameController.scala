package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey}
import models.game.Settings
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import repositories.WaitingRoomRepository
import services.game.BoardGenerator
import views.html.{WaitingRoom, Index}

import scala.concurrent.Future

class GameController @Inject()(waitingRoomView: WaitingRoom,
                               gameView: Index,
                               cc: ControllerComponents,
                               userAction: UserAction,
                               boardGenerator: BoardGenerator
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def waitingRoom(room: String): Action[AnyContent] = userAction {
    request =>
      Ok(waitingRoomView(room)).addingToSession(userIdKey -> request.userName, roomKey -> room)(request)
  }

  def startGame(room: String): Action[AnyContent] = userAction {
    val game = boardGenerator.create(Settings(23, 30, 10, 10, 30))
    WaitingRoomRepository.migrateToGameRoom(room, game)
    Redirect(routes.GameController.game())
  }

  def game() = userAction {
    Ok(gameView())
  }

  def socketWaitingRoom = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      (request.session.get(userIdKey), request.session.get(roomKey)) match {
        case (Some(user), Some(room)) =>
          Right(ActorFlow.actorRef { out =>
            WaitingRoomSocketActor.props(out, user, room)
          })
        case _ => Left(Forbidden)
      }
    )
  }

  def socketGameRoom = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(
      (request.session.get(userIdKey), request.session.get(roomKey)) match {
        case (Some(user), Some(room)) =>
          Right(ActorFlow.actorRef { out =>
            GameRoomSocketActor.props(out, user, room)
          })
        case _ => Left(Forbidden)
      }
    )
  }

}

object GameController {
  val roomKey = "roomname"
  val userIdKey = "username"
}
