package models.game

import java.util.UUID
import scala.util.Random

final case class Territory(hexes: Set[Hex], player: Int, diceCount: Int, id: String = UUID.randomUUID().toString) {

  def belongsTo(p: Player): Boolean = p.number == player

  def postAttack: Territory = this.copy(diceCount = 1)

  def beats(other: Territory): Boolean = {
    //todo actually roll dice. show this?
    Seq.fill(diceCount)(Random.nextInt(5)+1).sum > Seq.fill(other.diceCount)(Random.nextInt(5)+1).sum
  }

  def centreMosthex = hexes.maxBy(_.confirmedNeighbors(hexes).size)

  def topPx = centreMosthex.topPx
  def leftPx = centreMosthex.leftPx


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
