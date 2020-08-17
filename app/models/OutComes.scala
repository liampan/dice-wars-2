package models

object OutComes {

  case object Blank extends OutCome

  case object Success extends OutCome

  case object Failure extends OutCome

  case object Advantage extends OutCome

  case object Threat extends OutCome

  case object Triumph extends OutCome

  case object Despair extends OutCome

}
sealed trait OutCome