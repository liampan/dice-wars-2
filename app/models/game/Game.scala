package models.game

case class Settings(
                   numberOfRows: Int,
                   numberOfColumns: Int,
                   numberOfTeams: Int,
                   minTerritorySize: Int,
                   maxTerritorySize: Int
                   )

case class Team(number: Int)

// a hex is a individual 'square' on the board
// a territory is a collection of 3 to 7 hexes
// when two territories touch they form a 'united Territory'

case class Game(settings: Settings, boardState: Seq[Territory], turn: Int = 0) { //skip turn if player is out

  private def teams: Seq[Team] = Range.inclusive(1, settings.numberOfTeams).map(Team.apply)

  //(team, isTurn, stillIn)
  def turnStatus: Seq[(Team, Boolean, Boolean)] = teams.map{
    team => (team, (turn%settings.numberOfTeams)+1 == team.number, boardState.exists(_.team == team))
  }

  def skipTurn: Game = this.copy(turn = turn + 1)

  def hexes =
    boardState.flatMap{t =>
      t.hexes.map(_ -> t.team)
    }

}
