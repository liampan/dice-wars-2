package models

import models.Symbols.{Advantage, Success}

import scala.util.Random

sealed trait Die {

  val sides: List[DiceFace]
  //require(sides.nonEmpty)

  def roll: DiceFace = sides.apply(Random.nextInt(sides.size))
}

object Dice {

  //live else where?
  private val blank = DiceFace()
  private val singleSuccess = DiceFace(Success)
  private val singleAdvantage = DiceFace(Advantage)
  private val doubleAdvantage = DiceFace(Advantage, Advantage)
  private val successAdvantage = DiceFace(Success, Advantage)


  case object BlueBoost extends Die {
    override val sides = List(blank, blank, singleSuccess, singleAdvantage, doubleAdvantage, successAdvantage)
  }

  case object GreenAbility extends Die {
    override val sides = List()
  }

  case object YellowProficiency extends Die {
    override val sides = List()
  }


  case object BlackSetback extends Die {
    override val sides = List()
  }

  case object RedChallenge extends Die {
    override val sides = List()
  }


  case object PurpleDifficulty extends Die {
    override val sides = List()
  }

}

