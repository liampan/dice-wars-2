package services

import models.Symbol

class DiceCancellingService {

  def cancelDice(rolledSymbols: List[Symbol]): List[Symbol] = {
    def cancelLoop(itr: List[Symbol], current: List[Symbol]): List[Symbol] = {
      current match {
        case Nil => itr
        case h :: Nil => h :: itr
        case symbolToCancel :: tail =>
          symbolToCancel.opposite
            .flatMap {
              opp => current.find(opp == _).map { _ =>
                cancelLoop(itr,
                  tail diff List(opp))
              }
            }.getOrElse {
              val matchedSymbolList = current.takeWhile(_ == symbolToCancel)
              cancelLoop(matchedSymbolList ::: itr,
                tail diff matchedSymbolList)
            }
      }
    }
    cancelLoop(Nil, rolledSymbols.sorted)
  }
}
