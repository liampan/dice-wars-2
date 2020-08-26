package services

import models.Symbol
import Symbol._

class DiceCancellingService {

  def cancelDice(rolledSymbols: List[Symbol]): List[Symbol] = {
    def get(wanted: Symbol*): List[Symbol] = rolledSymbols.filter(wanted.contains)

    List(
      get(Advantage, Threat),
      get(Success, Failure),
      get(Triumph),
      get(Despair)
    ).flatMap(findMost)
  }

  def findMost(symbols: List[Symbol]): List[Symbol] =
    if (symbols.distinct.length < 2) symbols
    else {
    val counts = symbols.distinct.map(s => (s, symbols.count(_ == s)))
    List.fill(counts.maxBy(_._2)._2 - counts.minBy(_._2)._2)(counts.maxBy(_._2)._1)
    }

}
