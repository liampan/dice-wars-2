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
}

case class PlayerTeam(userId: String, number: Int) extends Team

case class AITeam(number: Int, userId: String = UUID.randomUUID().toString.takeRight(6)) extends Team {

  //todo how does AI play?
  def playTurn(game: Game): Game = {
    Thread.sleep(1000)
    game.endTurn(userId)
  }
}

case class Game(settings: Settings, boardState: Seq[Territory], teams: Seq[Team], turn: Int = 0) { //skip turn if player is out

  def thisTurn = teams.find((turn%settings.numberOfTeams)+1 == _.number).getOrElse(throw new Exception("Team is missing"))

  //(team, isTurn, stillIn)
  def turnStatus: Seq[(Team, Boolean, Boolean)] = teams.map{
    team => (team, (turn%settings.numberOfTeams)+1 == team.number, boardState.exists(_.team == team))
  }

  def isAITurn = thisTurn.isInstanceOf[AITeam]
  def thisTurnIsOut = !boardState.exists(_.team == thisTurn)


  def playThisAITurn =
    thisTurn.asInstanceOf[AITeam].playTurn(this)

  //todo
  // - distribute dice
  def endTurn(userId: String): Game =
    if (thisTurn.userId.toUpperCase == userId.toUpperCase)
      this.copy(turn = turn + 1)
    else this //this person should not have sent end turn.


  def skipTurn: Game = if(thisTurnIsOut) this.copy(turn = turn + 1) else this

}
