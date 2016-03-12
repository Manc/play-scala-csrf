package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf._
import play.api.Logger

@Singleton
class FormController @Inject() (addToken: CSRFAddToken, checkToken: CSRFCheck) extends Controller {

	def addtoken = addToken {
		Action { implicit req: RequestHeader =>
			val token: CSRF.Token = CSRF.getToken.get
			Ok(s"""
				|<!DOCTYPE html>
				|<html><head></head><body>
				|<form action="${routes.FormController.check}" method="post">
				|	<input type="text" name="${token.name}" value="${token.value}" size="90">
				|	<button type="submit">Submit</button>
				|</form>
				|</body>
				|""".stripMargin
			).as("text/html")
		}
	}

	def notoken = Action { implicit req: RequestHeader =>
		Ok(s"""
			|<!DOCTYPE html>
			|<html><head></head><body>
			|<form action="${routes.FormController.check}" method="post">
			|   <button type="submit">Submit</button>
			|</form>
			|</body>
			|""".stripMargin
		).as("text/html")
	}

	def check = checkToken {
		Action { implicit req: RequestHeader =>
			Logger.debug("The check action has been executed")
			Ok("Success")
		}
	}

}
