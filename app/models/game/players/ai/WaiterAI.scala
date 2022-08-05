package models.game.players.ai

import models.game.Game

import scala.util.Random


//sits and waits until turn a turn, then attacks everything until it is down to 3 dice, end turn and repeats
final case class WaiterAI(override val number: Int, waitUntil: Int = Random.between(50, 200)) extends AI {

  override val userName: String = super.userName + " waiter"

  override def playTurn(game: Game): Game = {
    if (game.turn < 100) game.endTurn
    else attack(game)
  }

  private def attack(game: Game): Game =
    game
      .boardState
      .filter(t => t.player == number && t.diceCount > 4 && t.attackable(game.boardState).nonEmpty)
      .maxByOption(_.diceCount)
      .fold(
        game.endTurn
      )(attacker => {
        val attackable = attacker.attackable(game.boardState).minBy(_.diceCount)
        game.attack(attacker.id, attackable.id)
      }
      )
}
