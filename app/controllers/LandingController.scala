package controllers

import actions.UserAction
import com.google.inject.Inject
import controllers.GameController.{roomKey, userIdKey, usernameKey}
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import views.html.landing.LandingPageView

class LandingController @Inject()(
                                   view: LandingPageView,
                                   cc: ControllerComponents,
                                   userAction: UserAction,
                                 ) extends AbstractController(cc) {


  val form: Form[String] = Form(
    "room" -> nonEmptyText(3, 16)
      .verifying("Only letters, numbers and hyphens", _.matches("^[\\w*\\d*-]*$"))
  )

  def onPageLoad(): Action[AnyContent] = userAction {
    implicit r =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = userAction {
    implicit r =>
      form.bindFromRequest()
        .fold(
          errorForm => Ok(view(errorForm)),
          room => Redirect(routes.GameController.waitingRoom(room))
        )
  }

}
