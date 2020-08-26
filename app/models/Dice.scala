package models

import models.DiceFaces._
import scala.util.Random

sealed trait Die {

  protected val sides: List[DiceFace]

  def roll: (Die, DiceFace) = (this, sides.apply(Random.nextInt(sides.size)))
}

object Dice {
  type RolledDice = List[(Die, DiceFace)]

  case object BlueBoost extends Die {
    override protected val sides = List(blank, blank, singleSuccess, singleAdvantage, doubleAdvantage, successAdvantage)
  }

  case object GreenAbility extends Die {
    override protected val sides = List(blank, doubleAdvantage, singleSuccess, singleAdvantage, doubleSuccess, successAdvantage, singleAdvantage, singleSuccess)
  }

  case object YellowProficiency extends Die {
    override protected val sides = List(blank, doubleSuccess, doubleSuccess, successAdvantage, successAdvantage, successAdvantage, singleSuccess, singleSuccess, doubleAdvantage, doubleAdvantage, singleAdvantage, triumph)
  }

  case object BlackSetback extends Die {
    override protected val sides = List(blank, blank, singleThreat, singleThreat, singleFailure, singleFailure)
  }

  case object RedChallenge extends Die {
    override protected val sides = List(blank, doubleFailure, doubleFailure, failureThreat, failureThreat, doubleThreat, doubleThreat, singleFailure, singleFailure, singleThreat, singleThreat, despair)
  }

  case object PurpleDifficulty extends Die {
    override protected val sides = List(blank, singleThreat, singleThreat, singleThreat, doubleThreat, singleFailure, doubleFailure, failureThreat)
  }

}
