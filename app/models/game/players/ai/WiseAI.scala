package models.game.players.ai

import models.game.Game

import scala.util.Random


//Only attacks territory it should beat
final case class WiseAI(override val number: Int, goCount: Int = 0) extends AI {

  override val userName: String = super.userName + " wise"

  override def playTurn(game: Game): Game = {
    val lts = game.largestUnitedTerritorySize(this).toDouble
    if (goCount < lts/8) attack(game) else endTurn(game)
  }

  private def endTurn(game: Game): Game =
    game
      .copy(players = game.players.updated(game.players.indexOf(this), copy(goCount = 0)))
      .endTurn

  private def attack(game: Game): Game =
    game
      .boardState
      .find(t => t.player == number && t.diceCount > 1 && t.attackable(game.boardState).exists(_.diceCount <= t.diceCount))
      .fold(endTurn(game))(attacker => {
        val attackable = attacker.attackable(game.boardState).minBy(_.diceCount)
        game
          .attack(attacker.id, attackable.id)
          .copy(players = game.players.updated(game.players.indexOf(this), copy(goCount = goCount + 1)))
      }
      )
}
