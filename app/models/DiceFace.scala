package models

import models.Symbols.{Advantage, Success}

sealed case class DiceFace(a: Option[Symbol], b: Option[Symbol]) {
  def symbols: List[Symbol] = a.toList ++ b.toList
}

private object DiceFace {
  def apply(): DiceFace = new DiceFace(None, None)
  def apply(a: Symbol): DiceFace = new DiceFace(Some(a), None)
  def apply(a: Symbol, b: Symbol): DiceFace = new DiceFace(Some(a), Some(b))
}

object DiceFaces {

  //TODO add all the needed ones of these:
  val blank = DiceFace()
  val singleSuccess = DiceFace(Success)
  val singleAdvantage = DiceFace(Advantage)
  val doubleAdvantage = DiceFace(Advantage, Advantage)
  val successAdvantage = DiceFace(Success, Advantage)
}