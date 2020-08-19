package controllers

import com.google.inject.Inject
import models.Dice._
import models.{DiceFace, Die, Symbol}
import play.api.mvc._
import services.DiceCancellingService
import views.html.diceroller

class DiceRollerController @Inject()(
                                      view: diceroller,
                                      cc: ControllerComponents,
                                      cancellingService: DiceCancellingService
                                    ) extends AbstractController(cc) {

  def onPageLoad(): Action[AnyContent] = Action {
    val diceToRoll = List(BlueBoost, GreenAbility, YellowProficiency, BlackSetback, RedChallenge, PurpleDifficulty) // TODO these would be determined by the request
    val rolledDice: RolledDice = diceToRoll.map(die => die.roll)
    val outcome = cancellingService.cancelDice(rolledDice.symbols)
    Ok(view(ViewModel(rolledDice, outcome)))
  }
}

case class ViewModel(rolledDice: RolledDice, outcome: List[Symbol]){
  private val outcomeScreenSize: Double = 64.0
  def outcomeScreenSize(die: Die, diceFace: DiceFace): Double =
    if(diceFace.symbols.size >= 2) (1.0/2.0) * outcomeScreenSize
    else Math.floor(die.screenSizeMultiplier * outcomeScreenSize)
  //TODO css refactor
  def imgSource(symbol: Symbol): String =
    s"/assets/images/dice/${symbol.toLowerCase}.png"

  def getDiceClass(die: Die, diceFace: DiceFace): String =
    die.name + diceFace.symbols.size
}
