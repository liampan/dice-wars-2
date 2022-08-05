package models.game.players

final case class Human(
                        userId: String,
                        userName: String,
                        number: Int,
                        clickedTerritoryId: Option[String] = None
                      ) extends Player {
  override def noClick: Player = this.copy(clickedTerritoryId = None)
}
