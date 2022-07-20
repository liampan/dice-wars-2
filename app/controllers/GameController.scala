package controllers

import actions.UserAction
import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey}
import models.game.{AI, Human, Player, Settings}
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

  //join/:room
  def waitingRoom(room: String): Action[AnyContent] = userAction {
    request =>
      Ok(waitingRoomView(room)).addingToSession(userIdKey -> request.userName, roomKey -> room)(request)
  }

  //this should be a post, from the start screen maybe?
  def startGame(room: String): Action[AnyContent] = userAction {
    val settings = Settings(23, 30, 10, 10, 30)
    val participants = WaitingRoomRepository.getRoom(room)
      .participants

    val players = Range.inclusive(1, settings.numberOfPlayers).map{playerNumber =>
      participants
        .toSeq
        .zipWithIndex
        .find(_._2+1 == playerNumber)
        .fold[Player](AI(playerNumber))(player => Human(player._1.userId, playerNumber))
    }

    val game = boardGenerator.create(settings, players)
    val newRoom = WaitingRoomRepository.migrateToGameRoom(room, game)
    newRoom.participants.foreach(_.actor ! "start-game")
    
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
