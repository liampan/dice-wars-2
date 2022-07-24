package models.auth

import play.api.mvc.{Request, WrappedRequest}

case class UserActionRequest[A](request: Request[A], userName: String, userId: String) extends WrappedRequest[A](request)
