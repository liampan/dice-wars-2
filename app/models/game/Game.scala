package models.game

import java.util.UUID

case class Settings(
                   numberOfRows: Int,
                   numberOfColumns: Int,
                   numberOfTeams: Int,
                   minTerritorySize: Int,
                   maxTerritorySize: Int
                   )

trait Team {
  val userId: String
  val number: Int
  val clickedTerritoryId: Option[String]
}

case class PlayerTeam(userId: String, number: Int, clickedTerritoryId: Option[String] = None) extends Team

case class AITeam(number: Int, userId: String = UUID.randomUUID().toString.takeRight(6)) extends Team {
  val clickedTerritoryId: Option[String] = None //do AI's need to click?

  //todo how does AI play?
  def playTurn(game: Game): Game = {
    Thread.sleep(1000)
    game.endTurn(userId)
  }
}

case class Game(settings: Settings, boardState: Seq[Territory], teams: Seq[Team], turn: Int = 0) { //skip turn if player is out

  def getTerritoryById(id: String): Option[Territory] = boardState.find(_.id == id)

  def rightPlayer(userId: String): Boolean = thisTurn.userId.toUpperCase == userId.toUpperCase

  def clickMine(userId: String, territoryId: String): Game = {
    val updated = thisTurn
      .asInstanceOf[PlayerTeam]
      .copy(clickedTerritoryId = Some(territoryId))
    copy(teams = teams.updated(teams.indexOf(thisTurn), updated))
  }

  //todo actually resolve dice
  def attack(userId: String, territoryId: String): Game = {
    val ownTerritory = boardState.find(_.id == thisTurn.clickedTerritoryId.getOrElse(throw new Exception(""))).getOrElse(throw new Exception("Enemy Territory is missing"))
    val enemyTerritory = boardState.find(_.id == territoryId).getOrElse(throw new Exception("Enemy Territory is missing"))
     if (ownTerritory.attackable(Set(enemyTerritory)).contains(enemyTerritory)) {
       val updated = enemyTerritory.copy(team = thisTurn.number)
       val player = thisTurn.asInstanceOf[PlayerTeam].copy(clickedTerritoryId = None)
       copy(
         boardState = boardState.updated(boardState.indexOf(enemyTerritory), updated),
         teams = teams.updated(teams.indexOf(thisTurn), player)
       )
     } else throw new Exception("trying to attack a non attack able territory")
  }

  def thisTurn = teams.find((turn%settings.numberOfTeams)+1 == _.number).getOrElse(throw new Exception("Team is missing"))

  //(team, isTurn, stillIn)
  def turnStatus: Seq[(Team, Boolean, Boolean)] = teams.map{
    team => (team, (turn%settings.numberOfTeams)+1 == team.number, boardState.exists(_.team == team.number))
  }

  def isAITurn = thisTurn.isInstanceOf[AITeam]
  def thisTurnIsOut = !boardState.exists(_.team == thisTurn.number)


  def playThisAITurn =
    thisTurn.asInstanceOf[AITeam].playTurn(this)

  //todo
  // - distribute dice
  def endTurn(userId: String): Game =
    if (rightPlayer(userId)) this.copy(turn = turn + 1)
    else this //this person should not have sent end turn.


  def skipTurn: Game = if(thisTurnIsOut) this.copy(turn = turn + 1) else this

}
