package controllers

import actions.UserAction
import com.google.inject.Inject
import controllers.GameController._
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import views.html.username.UserNamePageView

class UserNameController @Inject()(
                                  view: UserNamePageView,
                                  cc: ControllerComponents,
                                  userAction: UserAction,
                                 ) extends AbstractController(cc) {

  val form: Form[String] = Form(
    "name" -> nonEmptyText(1, 16)
      .verifying("No special characters", _.matches("^[\\w*\\s*\\d*&#]*$"))
      .verifying("Enter a name", _.trim.nonEmpty)
  )

  def onPageLoad(): Action[AnyContent] = Action {
    implicit r =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action {
    implicit r =>
      form.bindFromRequest()
        .fold(
          errorForm => Ok(view(errorForm)),
          name => Redirect(routes.LandingController.onPageLoad()).addingToSession(usernameKey -> name)
        )
  }

}
