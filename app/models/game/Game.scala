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

case class Game(settings: Settings, boardState: Set[Territory]) {

  def hexes =
    boardState.flatMap{t =>
      t.hexes.map(_ -> t.team)
    }

}
