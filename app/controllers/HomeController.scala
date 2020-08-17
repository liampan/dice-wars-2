package controllers

import com.google.inject.Inject
import play.api.mvc._
import views.html.index

class HomeController @Inject()(view: index,
                               cc: ControllerComponents
                              ) extends AbstractController(cc) {


  def onPageLoad(): Action[AnyContent] = Action {
    Ok(view())
  }


}
