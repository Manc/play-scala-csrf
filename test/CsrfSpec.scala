import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class CsrfSpec extends PlaySpec with OneAppPerTest {

	"FormController.check" should {

		"respond with status 403 with non-matching CSRF tokens" in {
			val headers = FakeHeaders(Seq(
				"Cookie" -> "PLAY_SESSION=0b0ef27b445139d156ced887ad5ced7b5b8228f0-csrfToken=e1a0c9af3a1243ea4f773f9939565c823bf73f4e-1457720224665-730b225601327ee6dab18793"
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"csrfToken" -> Seq("0000000000000000000000000000000000000000-0000000000000-000000000000000000000000")
			))

			val page = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(page) mustBe FORBIDDEN
		}

		"respond with status 200 with matching CSRF tokens in session cookie and body" in {
			// Get a CSRF token first...
			val formPage = route(app, FakeRequest(GET, "/form/addtoken")).get
			status(formPage) mustBe OK

			val sess: Session = session(formPage)
			val sessToken: Option[String] = sess.get("csrfToken")
			sessToken.isDefined mustBe true

			val cooks: Cookies = cookies(formPage)
			val optSessCookie: Option[Cookie] = cooks.get("PLAY_SESSION")
			optSessCookie.isDefined mustBe true

			// ...then use CSRF token in a POST request...
			val headers = FakeHeaders(Seq(
				("Cookie", "PLAY_SESSION=" + optSessCookie.get.value)
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"csrfToken" -> Seq(sessToken.get)
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe OK
		}

		"respond with status 403 when token in session cookie but not in body" in {
			// Get a CSRF token first...
			val formPage = route(app, FakeRequest(GET, "/form/addtoken")).get
			status(formPage) mustBe OK

			val sess: Session = session(formPage)
			val sessToken: Option[String] = sess.get("csrfToken")
			sessToken.isDefined mustBe true

			val cooks: Cookies = cookies(formPage)
			val optSessCookie: Option[Cookie] = cooks.get("PLAY_SESSION")
			optSessCookie.isDefined mustBe true

			// ...then use CSRF token in a POST request, but only in cookie...
			val headers = FakeHeaders(Seq(
				("Cookie", "PLAY_SESSION=" + optSessCookie.get.value)
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"notoken" -> Seq("just something else")
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 when token in body but not in cookie" in {
			// Get a CSRF token first...
			val formPage = route(app, FakeRequest(GET, "/form/addtoken")).get
			status(formPage) mustBe OK

			val sess: Session = session(formPage)
			val sessToken: Option[String] = sess.get("csrfToken")
			sessToken.isDefined mustBe true

			val cooks: Cookies = cookies(formPage)
			val optSessCookie: Option[Cookie] = cooks.get("PLAY_SESSION")
			optSessCookie.isDefined mustBe true

			// ...then use CSRF token in a POST request, but only in body...
			val headers = FakeHeaders(Seq(
				("Cookie", "NO_SESSION_COOKIE=something")
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"csrfToken" -> Seq(sessToken.get)
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 when no token is present at all, but other cookie and body" in {
			val headers = FakeHeaders(Seq(
				("Cookie", "NO_SESSION_COOKIE=something")
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"notoken" -> Seq("just something else")
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 when no token is present at all, no other cookie, but a body" in {
			val headers = FakeHeaders(Seq())

			val body = AnyContentAsFormUrlEncoded(Map(
				"notoken" -> Seq("just something else")
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 when no token is present at all, no body, but other cookie" in {
			val headers = FakeHeaders(Seq(
				("Cookie", "NO_SESSION_COOKIE=something")
			))

			val body = AnyContentAsFormUrlEncoded(Map())

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 when no token is present at all, but a body and a non-cookie header" in {
			val headers = FakeHeaders(Seq(
				("X-Custom-Header", "something")
			))

			val body = AnyContentAsFormUrlEncoded(Map(
				"notoken" -> Seq("just something else")
			))

			val checkPage = route(app, FakeRequest(POST, "/form/check", headers, body)).get
			status(checkPage) mustBe FORBIDDEN
		}

		"respond with status 403 without adding anything to request" in {
			val page = route(app, FakeRequest(POST, "/form/check")).get
			status(page) mustBe FORBIDDEN
		}

	}

}