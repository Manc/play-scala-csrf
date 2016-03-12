# Demonstration of CSRF issue

This project is only here to demonstrate a potential issue with the CSRF check in
[Play Framework](https://github.com/playframework/playframework) 2.5.0.

## Manual testing in browser

- Start application: `sbt run`
- Navigate to [http://localhost:9000/form/notoken](http://localhost:9000/form/notoken). This will not add a CSRF
  token to the session. Make sure there are no cookies present in the browser added by previous apps that ran on
  `localhost`.
- Submit the form via a `POST` request by clicking the *Submit* button.
- Due to the missing CSRF token you should get an error response, but you will see a success message.
- Now navigate to [http://localhost:9000/form/addtoken](http://localhost:9000/form/addtoken). This will add a
  CSRF token to the session and the form. (The CSRF token in the form has been put into an `input type="text"` field
  for easier manipulation. Usually this would be an `input type="hidden"` field.)
- Submit the form via a `POST` request by clicking the *Submit* button.
- You will correctly see a success message, because the CSRF tokens in the session cookie matched the token in the form.
- Go back to [http://localhost:9000/form/addtoken](http://localhost:9000/form/addtoken) (reload the page).
- Remove or manipulate the CSRF token in the input field and submit the form again.
- You will see the request is correctly rejected, because tokens in session and form did not match.
- Go back to [http://localhost:9000/form/addtoken](http://localhost:9000/form/addtoken) (reload the page).
- Delete all cookies for the `localhost` domain in your browser, e.g. using Google Chrome's inspector, select tab
  *Resources*, select *Cookies*, select `localhost`, select the cookie `PLAY_SESSION` (and any other cookies that might
  be there).
- Put anything you want into the input field of the form.
- Submit the form via a `POST` request by clicking the *Submit* button.
- Due to the missing CSRF token you should get an error response, but you will see a success message.

## Manual testing with Curl

Run the application

```
sbt run
```

Then post a request to the protected route, which should give you an error response, but doesn't:

```
curl -X POST http://localhost:9000/form/check
```

Or post a request to the protected route with any cookie, which will produce an error response:

```
curl -X POST --cookie "SOME_COOKIE=abc" http://localhost:9000/form/check
```

## Automated tests

I've added tests for all this in [test/CsrfSpec.scala](test/CsrfSpec.scala).

Start `sbt` and run `test-only CsrfSpec`. Three of the tests will fail; whenever there is no cookie in the header
of the `POST` request to the protected route, the action is executed.
