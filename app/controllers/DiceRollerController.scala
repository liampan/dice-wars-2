package controllers

import com.google.inject.Inject
import models.{DiceFace, Die}
import models.Dice.BlueBoost
import play.api.mvc._
import views.html.diceroller

class DiceRollerController @Inject()(
                                      view: diceroller,
                                      cc: ControllerComponents
                                    ) extends AbstractController(cc) {


  def onPageLoad(): Action[AnyContent] = Action {
    val diceToRoll = List(BlueBoost, BlueBoost, BlueBoost) // TODO these would be determined by the request
    val rolledDice: List[(Die, DiceFace)] = diceToRoll.map(die => (die, die.roll))
    val outCome = rolledDice.flatMap(_._2.symbols).distinct //TODO would be the logic of choosing the overall outcome from all the dice outcomes
    Ok(view(rolledDice, outCome))
  }

}
