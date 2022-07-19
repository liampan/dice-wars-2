package controllers

import actions.UserAction
import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey}
import models.game.{AITeam, PlayerTeam, Settings, Team}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Range => _, _}
import repositories.WaitingRoomRepository
import services.game.BoardGenerator
import views.html.{Index, WaitingRoom}

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
    val settings = Settings(23, 30, 10, 10, 30)
    val participants = WaitingRoomRepository.getRoom(room)
      .participants

    val teams = Range.inclusive(1, settings.numberOfTeams).map{teamNumber =>
      participants
        .toSeq
        .zipWithIndex
        .find(_._2+1 == teamNumber)
        .fold[Team](AITeam(teamNumber))(player => PlayerTeam(player._1.userId, teamNumber))
    }

    val game = boardGenerator.create(settings, teams)
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
