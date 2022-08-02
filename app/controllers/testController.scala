package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import models.game.{Game, Hex, Human, Settings, Territory}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.game.BoardGenerator
import views.html.game.{GameScreenView, HexPartial}
import views.html.Test

class testController @Inject()(testView: Test,
                               cc: ControllerComponents,
                               boardGenerator: BoardGenerator
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def test() = Action {
    val settings = Settings(8, 10, 50)

    val game = Game(Settings(1, 1, 1), boardState = Set(Territory(Set(Hex(1, 1), Hex(2, 2), Hex(2, 1)), 1, 1)), Seq(Human("pan", "pan", 1)))

    //val game = boardGenerator.create(settings, Seq(Human("pan", "pan", 1)))

    Ok(testView(HexPartial(game, "blah", false)))
  }

}