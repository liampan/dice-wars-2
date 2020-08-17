package models

import models.OutComes.{Advantage, Blank, Success}

import scala.util.Random

sealed trait Die {

  val sides: List[OutCome]

  def roll: OutCome = sides.apply(Random.nextInt(sides.size))
}

object Dice {

  case object BlueBoost extends Die {
    override val sides = List(Advantage, Advantage, Blank, Success, Success)
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

