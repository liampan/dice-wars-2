package models

import models.DiceFaces._
import scala.util.Random

sealed trait Die {

  protected val sides: List[DiceFace]

  def roll: DiceFace = sides.apply(Random.nextInt(sides.size))
}

object Dice {

  //TODO ADD ALL THE DICES' SIDES
  case object BlueBoost extends Die {
    override protected val sides = List(blank, blank, singleSuccess, singleAdvantage, doubleAdvantage, successAdvantage)
  }

  case object GreenAbility extends Die {
    override protected val sides = List(blank)
  }

  case object YellowProficiency extends Die {
    override protected val sides = List(blank)
  }

  case object BlackSetback extends Die {
    override protected val sides = List(blank)
  }

  case object RedChallenge extends Die {
    override protected val sides = List(blank)
  }

  case object PurpleDifficulty extends Die {
    override protected val sides = List(blank)
  }

}

