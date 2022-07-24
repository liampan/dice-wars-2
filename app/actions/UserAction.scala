package actions

import com.google.inject.{ImplementedBy, Inject}
import controllers.GameController.{userIdKey, usernameKey}
import controllers.routes
import models.auth
import models.auth.UserActionRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserActionImpl])
trait UserAction
  extends ActionRefiner[Request, UserActionRequest]
    with ActionBuilder[UserActionRequest, AnyContent]

class UserActionImpl @Inject()(
                                val parser: BodyParsers.Default,
                              )(implicit val executionContext: ExecutionContext) extends UserAction {

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserActionRequest[A]]] = {
    Future.successful(
        request.session.get(usernameKey)
          .fold[Either[Result, UserActionRequest[A]]](
            Left(Redirect(routes.UserNameController.onPageLoad()))
          )(username => Right[Result, UserActionRequest[A]](
            request.session.get(userIdKey).fold[UserActionRequest[A]] (
              UserActionRequest(request, username, UUID.randomUUID().toString.takeRight(5))
            )(
              userId => UserActionRequest(request, username, userId)
            )
          )
      )
    )
  }
}