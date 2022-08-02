package models.game

import services.game.BoardGenerator

import java.util.UUID
import scala.annotation.tailrec
import scala.util.Random

case class Settings(
                   numberOfPlayers: Int, //max 15 works
                   minTerritorySize: Int, // no smaller than 5 really
                   maxTerritorySize: Int //probs max 100/ the bigger the difference the more complete the map is
                   ){
  final val numberOfRows: Int = 29 //23
  final val numberOfColumns: Int = 35 // 30
}

trait Player {
  val userId: String
  val userName: String
  val number: Int
  val clickedTerritoryId: Option[String]
  def noClick: Player
}

case class Human(userId: String, userName: String, number: Int, clickedTerritoryId: Option[String] = None) extends Player{
  override def noClick: Player = this.copy(clickedTerritoryId = None)
}

//basic Ai attacks once per go.
case class AI(number: Int) extends Player {
  override val userName: String = "\uD835\uDE08\uD835\uDE10" // AI
  override val clickedTerritoryId: Option[String] = None
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

case class Attack(attacker: Territory, defender: Territory, attackDice: Seq[Int], defendDice: Seq[Int]){
  def attackerWin: Boolean = attackDice.sum > defendDice.sum
}

case class Game(settings: Settings, boardState: Set[Territory], players: Seq[Player], turn: Int = 0, lastAttack: Option[Attack] = None) { //skip turn if player is out

  def getTerritoryById(id: String): Option[Territory] = boardState.find(_.id == id)

  def rightPlayer(userId: String): Boolean = thisTurn.userId.toUpperCase == userId.toUpperCase

  def notify(userId: String): Boolean =
    rightPlayer(userId) &&
      thisTurn.clickedTerritoryId.isEmpty &&
      !lastAttack.exists(_.attacker.player == thisTurn.number)

  def clickMine(territoryId: String): Game = {
    val updated = thisTurn.asInstanceOf[Human].copy(clickedTerritoryId = Some(territoryId))
    copy(players = players.updated(players.indexOf(thisTurn), updated))
  }

  def attack(enemyTerritoryId: String): Game = {
    val ownTerritoryId = thisTurn.clickedTerritoryId.getOrElse(throw new Exception("No clickedTerritoryId"))
    attack(ownTerritoryId, enemyTerritoryId)
  }

  def attack(friendlyTerritoryId: String, enemyTerritoryId: String): Game = {
    val ownTerritory = boardState.find(_.id == friendlyTerritoryId).getOrElse(throw new Exception("Friendly Territory is missing"))
    val enemyTerritory = boardState.find(_.id == enemyTerritoryId).getOrElse(throw new Exception("Enemy Territory is missing"))
     if (ownTerritory.attackable(boardState).contains(enemyTerritory) && ownTerritory.diceCount > 1) {
       val updatedFriendly = ownTerritory.postAttack
       val attack = ownTerritory.attack(enemyTerritory)
       val updatedEnemy = if (attack.attackerWin) enemyTerritory.copy(player = thisTurn.number, diceCount = ownTerritory.diceCount -1) else enemyTerritory
       copy(
         boardState = boardState - enemyTerritory - ownTerritory + updatedEnemy + updatedFriendly,
         players = players.map(_.noClick),
         lastAttack = Some(attack)
       )
     } else throw new Exception(s"trying to attack a non attack able territory ${ownTerritory} -> ${enemyTerritory} = ${ownTerritory.attackable(Set(enemyTerritory))}")
  }

  def thisTurn: Player = players.find((turn%settings.numberOfPlayers)+1 == _.number).getOrElse(throw new Exception("Player is missing"))

  def largestUnitedTerritorySize(player: Player) = {
    val allPlayerTerritories = boardState.filter(_.player == player.number)
    val neightborsMap = allPlayerTerritories.map(t => t.id -> t.neighbors(allPlayerTerritories).map(_.id)).toMap

    @tailrec
    def loop(grouped: Set[String]): Int = {
      val neighbors = grouped.flatMap(t => neightborsMap(t)) ++ grouped
      if (neighbors == grouped) grouped.size
      else loop(grouped ++ neighbors)
    }

    allPlayerTerritories.maxByOption(t => loop(Set(t.id))).map(t => loop(Set(t.id))).getOrElse(0)

  }

  //(player, isTurn, stillIn, largestUnitedTerritoryCount, diceCount)
  def turnStatus: Seq[(Player, Boolean, Boolean, Int, Int)] = players.map{
    player =>
      val isTurn = (turn%settings.numberOfPlayers)+1 == player.number
      val stillPlaying = playerIsStillInPlay(player)
      val largestUnitedTerritoryCount = largestUnitedTerritorySize(player)
      val diceCount = boardState.filter(_.player == player.number).toSeq.map(_.diceCount).sum
      (player, isTurn, stillPlaying, largestUnitedTerritoryCount, diceCount)
  }

  def playerIsStillInPlay(player: Player) =
    boardState.exists(_.player == player.number)

  def humanPlayersLeft: Boolean =
    players.filter(_.isInstanceOf[Human]).exists(playerIsStillInPlay)

  def gameComplete: Boolean =
    boardState.map(_.player).size == 1 || !humanPlayersLeft

  def winnerPlayerNumber: Option[Int] = {
    val inPlay = boardState.map(_.player)
    if (inPlay.size == 1) inPlay.headOption else None
  }

  def playerNumberFormId(id: String): Int =
    players.find(_.userId == id).map(_.number)
      .getOrElse(throw new IllegalStateException(s"Player is missing $id"))


  def isAITurn: Boolean = thisTurn.isInstanceOf[AI]
  def thisTurnIsOut = !playerIsStillInPlay(thisTurn)


  def playThisAITurn =
    thisTurn.asInstanceOf[AI].playTurn(this)

  def endTurn: Game = {
    val dice: Int = largestUnitedTerritorySize(thisTurn)
    val playerTerritories: Set[Territory] = boardState.filter(_.player == thisTurn.number)

    def adder(territories: Seq[Territory], dice: Int): Set[Territory] =
      if(territories.forall(_.diceCount == 8) || dice == 0) territories.toSet
      else {
        val t = territories.find(_.diceCount < 8).get
        val newT = t.copy(diceCount = t.diceCount + 1)
        val newTerritories = territories.updated(territories.indexOf(t), newT)
        adder(Random.shuffle(newTerritories), dice - 1)
      }

    val added = adder(playerTerritories.toSeq, dice)
    val newBoardState = boardState.filterNot(_.player == thisTurn.number) ++ added

    this.copy(
      boardState = newBoardState,
      players = players.map(_.noClick),
      turn = turn + 1
    )
  }

  def skipTurn: Game = {
    if(thisTurnIsOut)
      this.copy(turn = turn + 1).skipTurn
    else this
  }

}
