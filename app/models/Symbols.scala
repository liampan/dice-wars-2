package models

object Symbols {

  case object Success extends Symbol

  case object Failure extends Symbol

  case object Advantage extends Symbol

  case object Threat extends Symbol

  case object Triumph extends Symbol

  case object Despair extends Symbol

}
sealed trait Symbol