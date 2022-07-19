package services.game

import com.google.inject.Inject
import models.game.{Game, Hex, Settings, Team, Territory}

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

  private def generateTerritories(settings: Settings, teams: Seq[Team]): Seq[Territory] = {
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

    val territories: Seq[Territory] = gen(initHexes.toSet, Seq.empty, 0)
      .filter(_.size >= settings.minTerritorySize)
      .zipWithIndex
      .map{case (hexes, i) => Territory(hexes, teams((i+1)%settings.numberOfTeams))}

    validateTerritories(territories).getOrElse(generateTerritories(settings, teams))
  }

  def validateTerritories(territory: Seq[Territory]): Option[Seq[Territory]] = {

    @tailrec
    def checkNeighbors(current: Seq[Territory], rest: Seq[Territory]): Boolean= {
      val next: Seq[Territory] = current.flatMap(_.neighbors(rest.toSet))
      if (current.isEmpty){
        rest.isEmpty
      } else {
        checkNeighbors(next, rest.filterNot(next.contains))
      }
    }
    Some(territory).filter(_ => checkNeighbors(Seq(territory.head), territory.tail))
  }

  def create(settings: Settings, teams: Seq[Team]): Game = {
    val territories = generateTerritories(settings, teams)

    Game(settings = settings, boardState = territories, teams = teams)
  }

}
