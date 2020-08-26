package models

import models.Symbol._

sealed case class DiceFace(a: Option[Symbol], b: Option[Symbol]) {
  def symbols: List[Symbol] = a.toList ++ b.toList
}

private object DiceFace {
  def apply(): DiceFace = new DiceFace(None, None)
  def apply(a: Symbol): DiceFace = new DiceFace(Some(a), None)
  def apply(a: Symbol, b: Symbol): DiceFace = new DiceFace(Some(a), Some(b))
}

object DiceFaces {
  val blank: DiceFace = DiceFace()

  val singleSuccess: DiceFace = DiceFace(Success)
  val doubleSuccess: DiceFace = DiceFace(Success, Success)
  val singleAdvantage: DiceFace = DiceFace(Advantage)
  val doubleAdvantage: DiceFace = DiceFace(Advantage, Advantage)
  val successAdvantage: DiceFace = DiceFace(Success, Advantage)
  val triumph: DiceFace = DiceFace(Triumph)

  val singleThreat: DiceFace = DiceFace(Threat)
  val doubleThreat: DiceFace = DiceFace(Threat, Threat)
  val singleFailure: DiceFace = DiceFace(Failure)
  val doubleFailure: DiceFace = DiceFace(Failure, Failure)
  val failureThreat: DiceFace = DiceFace(Failure, Threat)
  val despair: DiceFace = DiceFace(Despair)
}
