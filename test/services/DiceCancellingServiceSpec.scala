package services

import models.Symbol._
import org.scalatestplus.play._

class DiceCancellingServiceSpec extends PlaySpec {

  val SUT = new DiceCancellingService

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
      SUT.cancelDice(List(Triumph, Despair)) mustBe List(Despair, Triumph)
    }
  }

}