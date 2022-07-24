package services.game

import com.google.inject.Inject
import models.game.{Game, Hex, Player, Settings, Territory}
import services.game.BoardGenerator.splitDice

import scala.annotation.tailrec
import scala.util.{Random => ScalaRandom}

class BoardGenerator @Inject()(random: ScalaRandom = ScalaRandom) {

  private[services] def getRandomHex(hexes: Set[Hex]): Hex =
    hexes.toSeq(Math.max(0, random.nextInt(hexes.size)-1))


  //filter before this to remove any hex where it has < 3 confirmedAvailableNeighbors
  private[services] def groupHexes(settings: Settings)(availableHexes: Set[Hex]): Set[Hex] = {
    if (availableHexes.size > settings.minTerritorySize) {
      val startingHex = getRandomHex(availableHexes)

      val size = settings.minTerritorySize + random.nextInt(settings.maxTerritorySize - settings.minTerritorySize)

        Range(1, size).foldLeft(Set(startingHex)){case (hexes, i) =>
        val available = availableHexes -- hexes
        val neighbors = getRandomHex(hexes).confirmedNeighbors(available)
        if (neighbors.isEmpty) hexes else hexes + getRandomHex(neighbors)
        }
    } else {
      Set.empty
    }
  }

  def splitStartDice(averageCount: Double, territoryCount: Int): Seq[Int] = {
    val bonus = if (territoryCount < averageCount) 3 else 0
    val totalDicePool = (3 * territoryCount) + bonus
    splitDice(territoryCount, totalDicePool)
  }

  private def generateTerritories(settings: Settings): Set[Territory] = {
    val initHexes: Seq[Hex] = for {
      row <- Range.inclusive(0, settings.numberOfRows)
      column <- Range.inclusive(0, settings.numberOfColumns)
    } yield Hex(row, column)


    @tailrec
    def gen(available: Set[Hex], acc: Seq[Set[Hex]], failCount: Int): Seq[Set[Hex]] = {
      val ter = groupHexes(settings)(available)
      if (ter.isEmpty && failCount > 3) acc :+ ter
      else if (ter.isEmpty) gen(available -- ter, acc :+ ter, failCount + 1)
      else gen(available -- ter, acc :+ ter, 0)
    }

    val territories: Map[Int, Seq[Territory]] = gen(initHexes.toSet, Seq.empty, 0)
      .filter(_.size >= settings.minTerritorySize)
      .zipWithIndex
      .map{case (hexes, i) => Territory(hexes, (i%settings.numberOfPlayers)+1, 1)}
      .groupBy(_.player)

    val averageTerritorySize = territories.map(_._2.length).sum / settings.numberOfPlayers.toDouble

    val territoryWithDice = territories.flatMap{
      case (_, t) => splitStartDice(averageTerritorySize, t.length).zip(t).map{case (dice, territory) => territory.copy(diceCount = Math.max(1, dice))}
    }.toSet

    validateTerritories(territoryWithDice).getOrElse(generateTerritories(settings))
  }

  def validateTerritories(territory: Set[Territory]): Option[Set[Territory]] = {

    @tailrec
    def checkNeighbors(current: Set[Territory], rest: Set[Territory]): Boolean= {
      val next: Set[Territory] = current.flatMap(_.neighbors(rest))
      if (current.isEmpty){
        rest.isEmpty
      } else {
        checkNeighbors(next, rest.diff(next))
      }
    }

    Some(territory).filter(_ => checkNeighbors(Set(territory.head), territory))
  }

  def create(settings: Settings, players: Seq[Player]): Game = {
    val territories = generateTerritories(settings)

    Game(settings = settings, boardState = territories, players = players)
  }

}

object BoardGenerator {
  def splitDice(territoryCount: Int, totalDice: Int): Seq[Int] = {
    val _territoryCount = territoryCount - 1
    val diceAmounts = Seq.fill(_territoryCount)(1)
      .foldLeft(Seq(Seq.fill(totalDice)(1))){
        (seqs, _) =>
          val (a, b) = seqs.last.splitAt(ScalaRandom.nextInt(4) + 1)
          seqs.init ++ Seq(a, b)
      }.map(_.length)
    if (diceAmounts.exists(a => a > 8)) splitDice(territoryCount, totalDice)
    else diceAmounts
  }
}
