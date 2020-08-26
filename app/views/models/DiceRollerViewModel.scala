package views.models

import models.Dice.RolledDice
import models.{DiceFace, Die, Symbol}

import scala.collection.SortedMap

class DiceRollerViewModel(val rolledDice: RolledDice, outcome: List[Symbol]){
  private val outcomeScreenSize: Double = 64.0
  def outcomeScreenSize(die: Die, diceFace: DiceFace): Double =
    if(diceFace.symbols.size >= 2) (1.0/2.0) * outcomeScreenSize
    else Math.floor(die.screenSizeMultiplier * outcomeScreenSize)

  def imgSource(symbol: Symbol): String =
    s"/assets/images/dice/${symbol.toLowerCase}.png"

  def getDiceClass(die: Die, diceFace: DiceFace): String =
    die.name + diceFace.symbols.size

  def getSortedOutcome: SortedMap[Symbol, Int] =
    SortedMap(outcome.sorted.groupBy(identity).mapValues(_.size).toSeq: _*)

}