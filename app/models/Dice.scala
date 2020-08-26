package models

import models.DiceFaces._
import scala.util.Random

sealed trait Die {

  protected val sides: List[DiceFace]

  def roll: (Die, DiceFace) = (this, sides.apply(Random.nextInt(sides.size)))
  val screenSizeMultiplier: Double
  val name: String
}

object Dice {
  case object BlueBoost extends Die {
    override protected val sides = List(blank, blank, singleSuccess, singleAdvantage, doubleAdvantage, successAdvantage)
    override val screenSizeMultiplier: Double = 1.0
    override val name: String = "boost"
  }

  case object GreenAbility extends Die {
    override protected val sides = List(blank, doubleAdvantage, singleSuccess, singleAdvantage, doubleSuccess, successAdvantage, singleAdvantage, singleSuccess)
    override val screenSizeMultiplier: Double = 0.7
    override val name: String = "ability"
  }

  case object YellowProficiency extends Die {
    override protected val sides = List(blank, doubleSuccess, doubleSuccess, successAdvantage, successAdvantage, successAdvantage, singleSuccess, singleSuccess, doubleAdvantage, doubleAdvantage, singleAdvantage, triumph)
    override val screenSizeMultiplier: Double = 1.0
    override val name: String = "proficiency"
  }

  case object BlackSetback extends Die {
    override protected val sides = List(blank, blank, singleThreat, singleThreat, singleFailure, singleFailure)
    override val screenSizeMultiplier: Double = 1.0
    override val name: String = "setback"
  }

  case object RedChallenge extends Die {
    override protected val sides = List(blank, doubleFailure, doubleFailure, failureThreat, failureThreat, doubleThreat, doubleThreat, singleFailure, singleFailure, singleThreat, singleThreat, despair)
    override val screenSizeMultiplier: Double = 0.9
    override val name: String = "challenge"
  }

  case object PurpleDifficulty extends Die {
    override protected val sides = List(blank, singleThreat, singleThreat, singleThreat, doubleThreat, singleFailure, doubleFailure, failureThreat)
    override val screenSizeMultiplier: Double = 0.7
    override val name: String = "difficulty"
  }

  type RolledDice = List[(Die, DiceFace)]
  implicit class RolledDiceRich(rolledDice: RolledDice) {
    def symbols: List[Symbol] = rolledDice.flatMap(_._2.symbols)
  }

}
