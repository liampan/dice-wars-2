package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey, usernameKey}
import controllers.WaitingRoomController.form
import models.game.Settings
import models.game.players.{Human, Player}
import models.game.players.ai.{AI, BasicAI}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, nonEmptyText}
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
    implicit request =>
      Ok(waitingRoomView(room, form))
        .withSession(usernameKey -> request.userName, userIdKey -> request.userId, roomKey -> room)
  }

  //this should be a post, from the start screen maybe?
  def startGame(room: String): Action[AnyContent] = userAction {
    implicit request =>
    //val settings = Settings(8, 10, 50)
    //val settings = Settings(10, 10, 30)
    //val settings = Settings(3, 30, 50)

      form.bindFromRequest().fold(errorForm => Ok(waitingRoomView(room, errorForm)),
        choices => {
          val participants = WaitingRoomRepository.getRoom(room).participants

          val settings = Settings(choices.AICount + participants.size, choices.minSize, choices.maxSize)

          val players = Range.inclusive(1, settings.numberOfPlayers).map{playerNumber =>
            participants
              .toSeq
              .zipWithIndex
              .find(_._2+1 == playerNumber)
              .fold[Player](AI.getAI(playerNumber))(player => Human(player._1.userId, player._1.userName, playerNumber))
          }.take(16)

          val game = boardGenerator.create(settings, players)
          val newRoom = WaitingRoomRepository.migrateToGameRoom(room, game)
          newRoom.participants.foreach(_.actor ! "start-game")

          Redirect(routes.GameController.game())
            .addingToSession(
              usernameKey -> request.userName,
              userIdKey -> request.userId,
              roomKey -> room)(request)
        })
  }

}

object WaitingRoomController {

  case class SettingsForm(AICount: Int, minSize: Int, maxSize: Int)

  val form: Form[SettingsForm] = Form[SettingsForm](
    mapping(
    "ai-count" -> nonEmptyText(1, 2).transform[Int](_.toInt, _.toString),
    "min-size" -> nonEmptyText(1, 2).transform[Int](_.toInt, _.toString),
    "max-size" -> nonEmptyText(1, 3).transform[Int](_.toInt, _.toString)
    )(SettingsForm.apply)(SettingsForm.unapply)
      .verifying("dont use silly numbers ", a => a.AICount >= 0 && a.minSize > 0 && a.maxSize > 1)
      .verifying("min must be smaller or equal to max", a => a.minSize <= a.maxSize)
  )
}