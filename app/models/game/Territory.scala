package models.game

import java.util.UUID

final case class Territory(hexes: Set[Hex], player: Int, id: String = UUID.randomUUID().toString) {

  def belongsTo(p: Player): Boolean = p.number == player

  private val row = hexes.toSeq.map(_.row).groupBy(i => i).mapValues(_.size).maxBy(_._2)._1
  private val column = hexes.toSeq.map(_.column).groupBy(i => i).mapValues(_.size).maxBy(_._2)._1
  private def evenRow: Boolean = row % 2 == 0

  def topPx = (row * 28) + 22
  def leftPx = (column * 31) + (if(evenRow) 23 else 31)


  def potentialNeighbors: Set[Hex] = hexes.flatMap(_.potentialNeighbors) -- hexes

  def isTouching(other: Territory): Boolean =
    other.hexes.exists(potentialNeighbors.contains)

  def neighbors(allTerritories: Set[Territory]): Set[Territory] =
    allTerritories.filter(isTouching)

  def attackable(allTerritories: Set[Territory]): Set[Territory] =
    neighbors(allTerritories).filter(_.player != player)

  def addHex(hex: Hex): Territory =
    copy(hexes = hexes + hex)
}
