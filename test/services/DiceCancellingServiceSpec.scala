package services

import models.Symbol._
import org.scalatestplus.play._

class DiceCancellingServiceSpec extends PlaySpec {

  val SUT = new DiceCancellingService


  "findMost" must {
    "work with none" in {
      SUT.findMost(Nil) mustBe Nil
    }
    "work with one" in {
      SUT.findMost(List(Success)) mustBe List(Success)
    }

    "work with one of each" in {
      SUT.findMost(List(Success, Failure)) mustBe Nil
    }

    "work with Many" in {
      SUT.findMost(List(Success, Failure, Success)) mustBe List(Success)
    }
  }

  "Cancel" must {
    "remove none" when {
      "no dice to cancel" in {
        SUT.cancelDice(Nil) mustBe Nil
      }

      "only one dice" in {
        SUT.cancelDice(List(Success)) mustBe List(Success)
      }
    }

    "remove a cancelling pair" when {
      Seq(
        (Success, Failure),
        (Advantage,Threat)
      ).foreach{ case (a, b) =>
        s"there is a pair $a $b" in {
          SUT.cancelDice(List(a, b)) mustBe Nil
        }
      }

      "there is a remaining result" in {
        SUT.cancelDice(List(Success, Failure, Success)) mustBe List(Success)
      }
    }

    "not cancel triumph with despair" in {
      SUT.cancelDice(List(Triumph, Despair)) mustBe List(Triumph, Despair)
    }
  }

}