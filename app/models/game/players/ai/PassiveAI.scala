package models.game.players.ai

import models.game.Game

final case class PassiveAI(override val number: Int) extends AI {

  override val userName: String = super.userName + " passive"

  override def playTurn(game: Game): Game = game.endTurn
}
