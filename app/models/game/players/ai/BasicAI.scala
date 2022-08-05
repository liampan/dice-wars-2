package models.game.players.ai

import models.game.Game

final case class BasicAI(override val number: Int, goCount: Int = 0) extends AI {

  override val userName: String = super.userName + " basic"

  override def playTurn(game: Game): Game = {
    if (goCount > 2)
      game
        .copy(players = game.players.updated(game.players.indexOf(this), copy(goCount = 0)))
        .endTurn
    else
      attack(game)
  }

  private def attack(game: Game): Game =
    game
      .boardState
      .filter(t => t.player == number && t.diceCount > 1 && t.attackable(game.boardState).nonEmpty)
      .maxByOption(_.diceCount)
      .flatMap(ownTerritory =>
        ownTerritory.attackable(game.boardState)
          .minByOption(_.diceCount)
          .map(attackble =>
            game.attack(ownTerritory.id, attackble.id)
          ))
      .getOrElse(game)
      .copy(players = game.players.updated(game.players.indexOf(this), copy(goCount = goCount + 1)))
}
