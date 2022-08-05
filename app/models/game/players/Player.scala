package models.game.players

trait Player {
  val userId: String
  def userName: String
  val number: Int
  val clickedTerritoryId: Option[String]
  def noClick: Player
}
