package services.game

import models.game.{Hex, Settings}
import org.scalatestplus.play.PlaySpec

import scala.util.Random

class BoardGeneratorSpec extends PlaySpec {

  val fakeRandomGetLast: Random = new Random(){
    override def nextInt(n: Int): Int = n
  }

  val fakeRandomGetMiddle: Random = new Random(){
    override def nextInt(n: Int): Int = n / 2
  }

  val sut = new BoardGenerator(fakeRandomGetLast)

  "getRandomAvailableNeighbor" must {
    "get a neighbor" in {
      val hexes = Seq(Hex(0, 0), Hex(1, 1))
      sut.getRandomHex(hexes.toSet) mustBe Hex(1, 1)
    }
  }

  "groupHexes" must {
    "group 2" in {
      val settings = Settings(0, 0, 0, 1, 2)
      val available = Set(Hex(0,0), Hex(0,1), Hex(1,0))

      sut.groupHexes(settings)(available) mustBe Set(Hex(0,1), Hex(1,0))
    }
    "group 3 (all)" in {
      val settings = Settings(0, 0, 0, 1, 3)
      val available = Set(Hex(0,0), Hex(0,1), Hex(1,0))

      sut.groupHexes(settings)(available) mustBe available
    }
  }

}
