package models.game

import org.scalatestplus.play.PlaySpec

class HexSpec extends PlaySpec {

  val sut = Hex(1, 1)

  "potentialNeighbors" must {
    "for hex(1, 1)" when {
      Seq(
        ("left", (1, 0)),
        ("up-left", (0, 1)),
        ("up-right", (0, 2)),
        ("right", (1, 2)),
        ("down-right", (2, 2)),
        ("down-left", (2, 1))
      ).foreach { case (direction, (row, column)) =>
        s"have hex to $direction" in {
          val expected = Hex(row, column)
          assert(sut.potentialNeighbors.contains(expected))
        }
      }
    }

      "for hex(0, 1)" when {
        Seq(
          ("left", (0, 0)),
          ("up-left", (-1, 0)),
          ("up-right", (-1, 1)),
          ("right", (0, 2)),
          ("down-right", (1, 1)),
          ("down-left", (1, 0))
        ).foreach { case (direction, (row, column)) =>
          s"have hex to $direction" in {
            val expected = Hex(row, column)
            assert(Hex(0, 1).potentialNeighbors.contains(expected))
          }
        }
      }
  }

}
