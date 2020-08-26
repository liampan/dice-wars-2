package controllers

import com.google.inject.Inject
import models.Dice._
import play.api.mvc._
import services.DiceCancellingService
import views.html.diceroller
import views.models.DiceRollerViewModel

class DiceRollerController @Inject()(
                                      view: diceroller,
                                      cc: ControllerComponents,
                                      cancellingService: DiceCancellingService
                                    ) extends AbstractController(cc) {

  def onPageLoad(): Action[AnyContent] = Action {
    val diceToRoll = List(BlueBoost, GreenAbility, YellowProficiency, BlackSetback, RedChallenge, PurpleDifficulty) // TODO these would be determined by the request
    val rolledDice: RolledDice = diceToRoll.map(die => die.roll)
    val outcome = cancellingService.cancelDice(rolledDice.symbols)
    Ok(view(new DiceRollerViewModel(rolledDice, outcome)))
  }
}
