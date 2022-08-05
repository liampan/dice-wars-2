package models.game.players.ai

import models.game.players.Player
import models.game.Game

import java.util.UUID
import scala.util.Random

trait AI extends Player {
  override val number: Int
  override def userName: String = "\uD835\uDE08\uD835\uDE10" // AI
  override val clickedTerritoryId: Option[String] = None
  override final val userId: String = "AI_" + UUID.randomUUID().toString.takeRight(5)
  override final def noClick: Player = this

  def playTurn(game: Game): Game

}

object AI {
  val ai: Seq[Int => AI] = Seq(BasicAI(_), PassiveAI(_), WaiterAI(_), WiseAI(_))

  def getAI(i: Int): AI =
    ai(Random.nextInt(ai.length))(i)
}