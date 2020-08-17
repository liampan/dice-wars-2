package models

case class DiceFace(a: Option[Symbol], b: Option[Symbol]) {
  def symbols: List[Symbol] = a.toList ++ b.toList
}

object DiceFace {
  def apply(): DiceFace = new DiceFace(None, None)
  def apply(a: Symbol): DiceFace = new DiceFace(Some(a), None)
  def apply(a: Symbol, b: Symbol): DiceFace = new DiceFace(Some(a), Some(b))
}