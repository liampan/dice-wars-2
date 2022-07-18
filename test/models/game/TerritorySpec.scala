package models.game

import org.scalatestplus.play.PlaySpec

class TerritorySpec extends PlaySpec {

  val sut = Territory(Set(Hex(1, 1)), Team(1))

  "potentialNeighbors" must {
    "work for one hex" in {
      sut.potentialNeighbors mustBe Set((0, 1), (0,2), (1, 2), (1, 0), (2, 1), (2, 2)).map(Hex.tupled.apply)
    }
    "work for another hex" in {

      Set(Hex(0,2), Hex(0,0), Hex(1,1), Hex(-1,2), Hex(-1,1), Hex(1,2))
      Set(Hex(0,2), Hex(0,0), Hex(1,1), Hex(-1,0), Hex(-1,1), Hex(1,0))

      Territory(Set(Hex(0, 1)), Team(1)).potentialNeighbors mustBe Set((0, 0), (-1, 0), (-1, 1), (0, 2), (1, 1), (1, 0)).map(Hex.tupled.apply)
    }

    "work for two hex" in {
      val expected = Set((0, 0), (-1, 0), (-1, 1), (0, 2), (1, 2), (2, 2), (2, 1), (1, 0)).map(Hex.tupled.apply)
      val territory = Territory(Set(Hex(0, 1), Hex(1, 1)), Team(1))

      Set(Hex(0,2), Hex(0,0), Hex(2,2), Hex(-1,2), Hex(-1,1), Hex(1,2), Hex(2,1), Hex(1,0))
      Set(Hex(0,2), Hex(0,0), Hex(2,2), Hex(-1,0),  Hex(-1,1), Hex(1,2), Hex(2,1), Hex(1,0))

      territory.potentialNeighbors mustBe expected
    }
  }

  "isTouching" must {
    "find the touching hex" when {
      Seq(
        ("left", (1, 0)),
        ("up-left", (0, 1)),
        ("up-right", (0, 2)),
        ("right", (1, 2)),
        ("down-right", (2, 2)),
        ("down-left", (2, 1))
      ).foreach { case (direction, (row, column)) =>
        s"it is to the $direction" in {
          val touchingHex = Hex(row, column)

          assert(sut.isTouching(Territory(Set(touchingHex), Team(2))))
        }
      }
    }

    "not touch its self" in {
      sut.isTouching(sut) mustBe false
    }

    "be false when divided by a hex" in {
      val thisT = Territory(Set(Hex(1, 0)), Team(1))
      val otherT = Territory(Set(Hex(1, 2)), Team(2))

      thisT.isTouching(otherT) mustBe false
    }

    "work for multiples" when {
      "it contains more than 1" in {
        val thisT = Territory(Set(Hex(1, 1), Hex(1, 2)), Team(1))
        val otherT = Territory(Set(Hex(1, 3), Hex(1, 4)), Team(2))

        assert(thisT.isTouching(otherT))
      }
      "when it touches more than once" in {
        val thisT = Territory(Set(Hex(0, 0), Hex(0, 1)), Team(1))
        val otherT = Territory(Set(Hex(1, 0)), Team(2))

        assert(thisT.isTouching(otherT))
      }
    }
  }

  "neighbors" must {
    "return the neighboring teams" when {
      "complex" in {
        val territory1 = Territory(Set(Hex(0, 0), Hex(0, 1), Hex(1, 0)), Team(1))
        val territory2 = Territory(Set(Hex(0, 2), Hex(1,1), Hex(2, 1)), Team(2))
        val territory3 = Territory(Set(Hex(1, 2), Hex(2, 2)), Team(3))
        val all = Set(territory1, territory2, territory3)

        withClue("1 should be neighbors with 2") {
          territory1.neighbors(all).map(_.team.number) mustBe Set(2)
        }
        withClue("2 should be neighbors with 1 and 3") {
          territory2.neighbors(all).map(_.team.number) mustBe Set(1, 3)
        }
        withClue("3 should be neighbors with 2") {
          territory3.neighbors(all).map(_.team.number) mustBe Set(2)
        }
      }
    }
  }

  "attackable" must {
    "return the neighboring enemy teams" when {
      "complex" in {
        val territory1 = Territory(Set(Hex(0, 0), Hex(0, 1), Hex(1, 0)), Team(1))
        val territory2 = Territory(Set(Hex(0, 2), Hex(1,1), Hex(2, 1)), Team(2))
        val territory3 = Territory(Set(Hex(1, 2), Hex(2, 2)), Team(1))
        val all = Set(territory1, territory2, territory3)

        withClue("1 should be able to attack 2") {
          territory1.neighbors(all) mustBe Set(territory2)
        }
        withClue("2 should be able to attack 1 and 3") {
          territory2.neighbors(all) mustBe Set(territory1, territory3)
        }
        withClue("3 should be able to attack 2") {
          territory3.neighbors(all) mustBe Set(territory2)
        }
      }
    }
  }

}
