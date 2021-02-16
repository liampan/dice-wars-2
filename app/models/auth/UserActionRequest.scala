package models.auth

import play.api.mvc.{Request, WrappedRequest}

case class UserActionRequest[A](request: Request[A], userName: String) extends WrappedRequest[A](request)
