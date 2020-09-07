package services

import models.Symbol

class DiceCancellingService {

  def cancelDice(rolledSymbols: List[Symbol]): List[Symbol] = {
    def get(wanted: Seq[Symbol]): List[Symbol] = rolledSymbols.filter(wanted.contains)

    rolledSymbols
      .distinct
        .map(cur => (cur, get(cur.opposite.toSeq :+ cur)))
        .foldLeft[List[(Symbol, List[Symbol])]](Nil) { (acc, cur) =>
          if (cur._1.opposite.exists(c => acc.map(_._1).contains(c))) acc
          else acc :+ cur
        }.flatMap(t => findMost(t._2))
  }

  def findMost(symbols: List[Symbol]): List[Symbol] =
    if (symbols.distinct.length < 2) symbols
    else {
    val counts = symbols.distinct.map(s => (s, symbols.count(_ == s)))
    List.fill(counts.maxBy(_._2)._2 - counts.minBy(_._2)._2)(counts.maxBy(_._2)._1)
    }

}
