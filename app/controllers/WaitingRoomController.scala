package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey, usernameKey}
import models.game.{AI, Human, Player, Settings}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import repositories.WaitingRoomRepository
import services.game.BoardGenerator
import views.html.waitingRoom.WaitingRoomView

class WaitingRoomController @Inject()(waitingRoomView: WaitingRoomView,
                                      cc: ControllerComponents,
                                      userAction: UserAction,
                                      boardGenerator: BoardGenerator
                                     )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  //join/:room
  def waitingRoom(room: String): Action[AnyContent] = userAction {
    request =>
      Ok(waitingRoomView(room))
        .addingToSession(usernameKey -> request.userName, userIdKey -> request.userId, roomKey -> room)(request)
  }

  //this should be a post, from the start screen maybe?
  def startGame(room: String): Action[AnyContent] = userAction {
    val settings = Settings(23, 30, 2, 10, 30)
    //val settings = Settings(23, 30, 10, 10, 30)
    //val settings = Settings(23, 30, 3, 30, 50)
    val participants = WaitingRoomRepository.getRoom(room)
      .participants

    val players = Range.inclusive(1, settings.numberOfPlayers).map{playerNumber =>
      participants
        .toSeq
        .zipWithIndex
        .find(_._2+1 == playerNumber)
        .fold[Player](AI(playerNumber))(player => Human(player._1.userId, player._1.userName, playerNumber))
    }

    val game = boardGenerator.create(settings, players)
    val newRoom = WaitingRoomRepository.migrateToGameRoom(room, game)
    newRoom.participants.foreach(_.actor ! "start-game")

    Redirect(routes.GameController.game())
  }

}
