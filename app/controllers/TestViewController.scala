package controllers

import com.google.inject.Inject
import models.game.Settings
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.game.BoardGenerator
import views.html.game.HexView

class TestViewController @Inject()(view: HexView,
                                   cc: ControllerComponents,
                                   boardGenerator: BoardGenerator
                                  ) extends AbstractController(cc) {

  def onPageLoad(): Action[AnyContent] = Action {
    val game = boardGenerator.create(Settings(23, 30, 10, 10, 30))
//    val game = boardGenerator.create(Settings(10, 15, 2, 5, 10))
    Ok(view(game))
  }


}