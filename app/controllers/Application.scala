package controllers

import models.Dice.BlueBoost
import play.api.mvc._

object Application extends Controller {

  def old = Action {

    Ok(views.html.index(BlueBoost.roll.toString()))
  }

  def index() = Action {
    val diceToRoll = List(BlueBoost, BlueBoost, BlueBoost) // these would be deteminded by the request
    val rolledDice = diceToRoll.map(_.roll)
    val outCome = rolledDice.flatMap(_.symbols) //would be the logic of choosing the overall outcome from all the dice outcomes
    Ok(views.html.index(outCome.toString()))
  }

}
