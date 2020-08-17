package controllers

import com.google.inject.Inject
import models.{DiceFace, Die}
import models.Dice._
import play.api.mvc._
import views.html.diceroller
import models.Symbol

class DiceRollerController @Inject()(
                                      view: diceroller,
                                      cc: ControllerComponents
                                    ) extends AbstractController(cc) {


  def onPageLoad(): Action[AnyContent] = Action {
    val diceToRoll = List(BlueBoost, YellowProficiency, RedChallenge) // TODO these would be determined by the request
    val rolledDice: List[(Die, DiceFace)] = diceToRoll.map(die => die.roll)
    val outcome = Outcome(rolledDice.flatMap(_._2.symbols)).cancelDice()
    Ok(view(rolledDice, outcome))
  }
}
//TODO move an better name and stuff
case class Outcome(symbols: List[Symbol]) {
  def cancelDice(): List[Symbol] = {
    def cancelLoop(itr: List[Symbol], current: List[Symbol]): List[Symbol] = {
      current match {
        case Nil => itr
        case h :: Nil => h :: itr
        case symbol :: tail =>
          symbol.opposite
            .flatMap {
              opp => current.find(opp == _).map { _ =>
                cancelLoop(itr,
                  tail diff List(opp))
              }
            }.getOrElse {
            val diff = current.takeWhile(_ == symbol)
            cancelLoop(diff ::: itr,
              tail diff diff)
          }
      }
    }
    cancelLoop(Nil, symbols.sorted)
  }
}
