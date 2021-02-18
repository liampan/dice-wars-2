package actions

import com.google.inject.{ImplementedBy, Inject}
import models.auth
import models.auth.UserActionRequest
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserActionImpl])
trait UserAction
  extends ActionRefiner[Request, UserActionRequest]
    with ActionBuilder[UserActionRequest, AnyContent]

class UserActionImpl @Inject()(
                                val parser: BodyParsers.Default,
                              )(implicit val executionContext: ExecutionContext)
  extends UserAction {

  private val logger: Logger = Logger(getClass)

  var counter: Int = 0
  def count: Int = {counter +=1; counter}

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserActionRequest[A]]] =
    Future.successful(Right(auth.UserActionRequest(request, "pan" + count))) //todo some actual log in where users are bespoke


}