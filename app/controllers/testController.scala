package controllers

import actions.UserAction
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import models.game.players.Human
import models.game.{Game, Hex, Settings, Territory}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.game.BoardGenerator
import views.html.game.{GameScreenView, HexPartial}
import views.html.Test
import views.html.game.Hexagon2

class testController @Inject()(testView: Test,
                               cc: ControllerComponents,
                               boardGenerator: BoardGenerator
                              )(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def test() = Action {
    val settings = Settings(4, 10, 50)


    //val game = Game(Settings(1, 1, 1), boardState = Set(Territory(Set((1,0), (0,1), (0,2), (1,2), (2,2), (2,1)).map(Hex.tupled), 1, 1)), Seq(Human("pan", "pan", 1)))
    //val game = Game(Settings(1, 1, 1), boardState = Set(Territory(Set((1,0), (1, 1)).map(Hex.tupled), 1, 1)), Seq(Human("pan", "pan", 1)))

    val game = boardGenerator.create(settings, Seq(Human("pan", "pan", 1), Human("notpan", "notpan", 2)))

    Ok(testView(HexPartial(game, "notpan", false)))
  }

}