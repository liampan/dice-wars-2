package controllers

import models.Dice.{BlueBoost}
import play.api.mvc._

object Application extends Controller {

  def index = Action {

    Ok(views.html.index(BlueBoost.roll.toString()))
  }

  def onSubmit() = Action {
    val diceToRoll = List(BlueBoost) // these would be deteminded by the request
    val rolledDice = diceToRoll.map(_.roll)
    val outCome = rolledDice.head //would be the logic of choosing the overall outcome from all the dice outcomes
    Ok(views.html.index(BlueBoost.roll.toString()))
  }

}
