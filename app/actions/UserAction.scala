package actions

import com.google.inject.{ImplementedBy, Inject}
import models.auth
import models.auth.UserActionRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserActionImpl])
trait UserAction
  extends ActionTransformer[Request, UserActionRequest]
    with ActionBuilder[UserActionRequest, AnyContent]

class UserActionImpl @Inject()(
                                val parser: BodyParsers.Default,
                              )(implicit val executionContext: ExecutionContext)
  extends UserAction {
  override protected def transform[A](request: Request[A]): Future[UserActionRequest[A]] = {
    request.session.get("username").fold[Future[UserActionRequest[A]]] {
      Future.successful(UserActionRequest(request, UUID.randomUUID().toString.takeRight(5)))
    }{
      username => Future.successful(UserActionRequest(request, username))
    }
  }
}