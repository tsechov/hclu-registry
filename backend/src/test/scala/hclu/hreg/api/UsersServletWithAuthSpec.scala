package hclu.hreg.api

import java.util.UUID
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import hclu.hreg.BaseServletSpec
import hclu.hreg.service.model.UserJson
import hclu.hreg.service.user.UserService
import hclu.hreg.test.UserTestHelpers
import org.mockito.Matchers
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatra.auth.Scentry
import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.JsonDSL._

import scala.concurrent.Future

class UsersServletWithAuthSpec extends BaseServletSpec with UserTestHelpers {

  def onServletWithMocks(authenticated: Boolean, testToExecute: (UserService, Scentry[UserJson]) => Unit) {
    val userService = mock[UserService]

    val servlet: MockUsersServlet = new MockUsersServlet(userService, authenticated)
    addServlet(servlet, "/*")

    testToExecute(userService, servlet.mockedScentry)
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated(any[HttpServletRequest], any[HttpServletResponse]) // before() and get('/logout')
        verify(mock).logout()(any[HttpServletRequest], any[HttpServletResponse])
        verifyZeroInteractions(userService)
      })
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    onServletWithMocks(authenticated = false, testToExecute = (userService, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated(any[HttpServletRequest], any(classOf[HttpServletResponse])) // before() and get('/logout')
        verify(mock, never).logout()
        verifyZeroInteractions(userService)
      })
  }

  "GET /" should "return user information" in {
    onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
      get("/") {
        status should be (200)
        body should be ("{\"id\":\"" + uuidStr +
          "\",\"login\":\"Jas Kowalski\",\"email\":\"kowalski@kowalski.net\"," +
          "\"token\":\"token1\",\"createdOn\":\"20150603T132503.000Z\"}")
      })
  }

  "POST /register" should "register new user" in {

    onServletWithMocks (authenticated = true, testToExecute = (userService, mock) => {
      when(userService.isUserDataValid(Some("newUser"), Some("newUser@sml.com"), Some("secret"))).thenReturn(true)
      when(userService.checkUserExistenceFor("newUser", "newUser@sml.com")).thenReturn(Future.successful(Right()))
      when(userService.registerNewUser("newUser", "newUser@sml.com", "secret", "first", "last")).thenReturn(Future.successful())
      post("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret", "firstname" -> "first", "lastname" -> "last")),
        defaultJsonHeaders) {

        status should be(201)
      }
    })

  }

}

