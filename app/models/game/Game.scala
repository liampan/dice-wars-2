package models.game

import services.game.BoardGenerator

import java.util.UUID
import scala.annotation.tailrec

case class Settings(
                   numberOfRows: Int,
                   numberOfColumns: Int,
                   numberOfPlayers: Int,
                   minTerritorySize: Int,
                   maxTerritorySize: Int
                   )

trait Player {
  val userId: String
  val number: Int
  val clickedTerritoryId: Option[String]
  val isAI: Boolean
  def noClick: Player
}

case class Human(userId: String, number: Int, clickedTerritoryId: Option[String] = None) extends Player{
  override val isAI: Boolean = false
  override def noClick: Player = this.copy(clickedTerritoryId = None)
}

//basic Ai attacks once per go.
case class AI(number: Int) extends Player {
  override val clickedTerritoryId: Option[String] = None
  override val isAI: Boolean = true
  override val userId: String = "AI_" + UUID.randomUUID().toString.takeRight(5)
  override def noClick: Player = this

  //todo how does AI play?
  def playTurn(game: Game): Game = {
    Thread.sleep(700)
    game
      .boardState
      .find(t => t.player == number && t.diceCount > 1)
      .flatMap(ownTerritory =>
        ownTerritory.attackable(game.boardState.toSet)
        .headOption
        .map(attackble =>
          game.attack(ownTerritory.id, attackble.id)
      ))
      .getOrElse(game)
      .endTurn
  }
}


case class Game(settings: Settings, boardState: Seq[Territory], players: Seq[Player], turn: Int = 0) { //skip turn if player is out

  def getTerritoryById(id: String): Option[Territory] = boardState.find(_.id == id)

  def rightPlayer(userId: String): Boolean = thisTurn.userId.toUpperCase == userId.toUpperCase

  def clickMine(territoryId: String): Game = {
    val updated = thisTurn.asInstanceOf[Human].copy(clickedTerritoryId = Some(territoryId))
    copy(players = players.updated(players.indexOf(thisTurn), updated))
  }

  def attack(enemyTerritoryId: String): Game = {
    val ownTerritoryId = thisTurn.clickedTerritoryId.getOrElse(throw new Exception(""))
    attack(ownTerritoryId, enemyTerritoryId)
  }

  //todo actually resolve dice
  def attack(friendlyTerritoryId: String, enemyTerritoryId: String): Game = {
    val ownTerritory = boardState.find(_.id == friendlyTerritoryId).getOrElse(throw new Exception("Friendly Territory is missing"))
    val enemyTerritory = boardState.find(_.id == enemyTerritoryId).getOrElse(throw new Exception("Enemy Territory is missing"))
     if (ownTerritory.attackable(Set(enemyTerritory)).contains(enemyTerritory) && ownTerritory.diceCount > 1) {
       val updatedFriendly = ownTerritory.postAttack
       val updatedEnemy = if (ownTerritory.beats(enemyTerritory)) enemyTerritory.copy(player = thisTurn.number, diceCount = ownTerritory.diceCount -1) else enemyTerritory
       val player = thisTurn.noClick
       copy(
         boardState = boardState.updated(boardState.indexOf(enemyTerritory), updatedEnemy).updated(boardState.indexOf(ownTerritory), updatedFriendly),
         players = players.updated(players.indexOf(thisTurn), player)
       )
     } else throw new Exception("trying to attack a non attack able territory")
  }

  def thisTurn: Player = players.find((turn%settings.numberOfPlayers)+1 == _.number).getOrElse(throw new Exception("Player is missing"))

  def largestUnitedTerritory(player: Player) = {
    val allPlayerTerritories = boardState.filter(_.player == player.number).toSet

    @tailrec
    def loop(grouped: Set[Territory]): Set[Territory] = {
      val neighbors = grouped.flatMap(_.neighbors(allPlayerTerritories)) ++ grouped
      if (neighbors == grouped) grouped
      else loop(grouped ++ neighbors)
    }

    allPlayerTerritories
      .map(t => loop(Set(t)))
      .maxByOption(_.size)
      .getOrElse(Set.empty)
      .toSeq
  }

  //(player, isTurn, stillIn, largestUnitedTerritoryCount)
  def turnStatus: Seq[(Player, Boolean, Boolean, Int)] = players.map{
    player =>
      val isTurn = (turn%settings.numberOfPlayers)+1 == player.number
      val stillPlaying = playerIsStillInPlay(player)
      val largestUnitedTerritoryCount = largestUnitedTerritory(player).size
      (player, isTurn, stillPlaying, largestUnitedTerritoryCount)
  }

  def playerIsStillInPlay(player: Player) =
    boardState.exists(_.player == player.number)

  def humanPlayersLeft: Boolean =
    players.filter(_.isInstanceOf[Human]).exists(playerIsStillInPlay)

  def gameComplete: Boolean =
    boardState.map(_.player).distinct.size == 1

  def isAITurn: Boolean = thisTurn.isAI
  def thisTurnIsOut = !playerIsStillInPlay(thisTurn)


  def playThisAITurn =
    thisTurn.asInstanceOf[AI].playTurn(this)

  def endTurn: Game = {
    val dice: Int = largestUnitedTerritory(thisTurn).size
    val playerTerritories: Seq[Territory] = boardState.filter(_.player == thisTurn.number)

    def adder(territories: Seq[Territory], dice: Int): Seq[Territory] = {
      val dicePool = BoardGenerator.splitDice(playerTerritories.length, dice)
      val a: Seq[(Territory, Int)] = territories.zip(dicePool).map{
        case (territory, dice) => if ((territory.diceCount + dice) <= 8)
          (territory.copy(diceCount = territory.diceCount + dice), 0)
        else (territory.copy(diceCount = 8), dice + territory.diceCount - 8)
      }
      val t = a.map(_._1)
      val d = a.map(_._2)
      if (d.sum == 0 || t.forall(_.diceCount == 8)) t
      else t ++ adder(t.filterNot(_.diceCount == 8), d.sum)
    }

    this.copy(
      boardState = boardState.filterNot(_.player == thisTurn.number) ++ adder(playerTerritories, dice),
      turn = turn + 1
    )
  }

  def skipTurn: Game = if(thisTurnIsOut) this.copy(turn = turn + 1) else this

}
