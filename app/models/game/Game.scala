package models.game

import java.util.UUID

case class Settings(
                   numberOfRows: Int,
                   numberOfColumns: Int,
                   numberOfTeams: Int,
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

case class Human(userId: String, number: Int, clickedTerritoryId: Option[String] = None, isAI: Boolean = false) extends Player{
  override def noClick: Player = this.copy(clickedTerritoryId = None)
}

case class AI(number: Int) extends Player {
  override val clickedTerritoryId: Option[String] = None
  override val isAI: Boolean = true
  override val userId: String = "AI_" + UUID.randomUUID().toString.takeRight(5)
  override def noClick: Player = this

  //todo how does AI play?
  def playTurn(game: Game): Game = {
    Thread.sleep(700)
    val ownTerritory = game.boardState.filter(_.team == number).head
    ownTerritory.attackable(game.boardState.toSet)
      .headOption
      .map(attackble =>
        game.attack(ownTerritory.id, attackble.id)
    )
      .getOrElse(game)
      .endTurn(userId)
  }
}


case class Game(settings: Settings, boardState: Seq[Territory], teams: Seq[Player], turn: Int = 0) { //skip turn if player is out

  def getTerritoryById(id: String): Option[Territory] = boardState.find(_.id == id)

  def rightPlayer(userId: String): Boolean = thisTurn.userId.toUpperCase == userId.toUpperCase

  def clickMine(userId: String, territoryId: String): Game = {
    val updated = thisTurn.asInstanceOf[Human].copy(clickedTerritoryId = Some(territoryId))
    copy(teams = teams.updated(teams.indexOf(thisTurn), updated))
  }

  def attack(enemyTerritoryId: String): Game = {
    val ownTerritoryId = thisTurn.clickedTerritoryId.getOrElse(throw new Exception(""))
    attack(ownTerritoryId, enemyTerritoryId)
  }

  //todo actually resolve dice
  def attack(friendlyTerritoryId: String, enemyTerritoryId: String): Game = {
    val ownTerritory = boardState.find(_.id == friendlyTerritoryId).getOrElse(throw new Exception("Friendly Territory is missing"))
    val enemyTerritory = boardState.find(_.id == enemyTerritoryId).getOrElse(throw new Exception("Enemy Territory is missing"))
     if (ownTerritory.attackable(Set(enemyTerritory)).contains(enemyTerritory)) {
       val updated = enemyTerritory.copy(team = thisTurn.number)
       val player = thisTurn.noClick
       copy(
         boardState = boardState.updated(boardState.indexOf(enemyTerritory), updated),
         teams = teams.updated(teams.indexOf(thisTurn), player)
       )
     } else throw new Exception("trying to attack a non attack able territory")
  }

  def thisTurn: Player = teams.find((turn%settings.numberOfTeams)+1 == _.number).getOrElse(throw new Exception("Team is missing"))

  //(team, isTurn, stillIn)
  def turnStatus: Seq[(Player, Boolean, Boolean)] = teams.map{
    team => (team, (turn%settings.numberOfTeams)+1 == team.number, teamIsStillInPlay(team))
  }

  def teamIsStillInPlay(team: Player) =
    boardState.exists(_.team == team.number)

  def humanPlayersLeft: Boolean =
    teams.filter(_.isInstanceOf[Human]).exists(teamIsStillInPlay)

  def gameComplete: Boolean =
    boardState.map(_.team).distinct.size == 1

  def isAITurn: Boolean = thisTurn.isAI
  def thisTurnIsOut = !teamIsStillInPlay(thisTurn)


  def playThisAITurn =
    thisTurn.asInstanceOf[AI].playTurn(this)

  //todo
  // - distribute dice
  def endTurn(userId: String): Game =
    if (rightPlayer(userId)) this.copy(turn = turn + 1)
    else this //this person should not have sent end turn.


  def skipTurn: Game = if(thisTurnIsOut) this.copy(turn = turn + 1) else this

}
