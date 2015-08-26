package hclu.hreg.api
import java.util.UUID
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import hclu.hreg.service.model.UserJson
import hclu.hreg.service.user.UserService
import hclu.hreg.test.UserTestHelpers
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatra.auth.Scentry
import scala.concurrent.ExecutionContext.Implicits.global
class MockUsersServlet(userService: UserService, authenticated: Boolean = true) extends UsersServlet(userService) with MockitoSugar with UserTestHelpers {

  val mockedScentry = mock[Scentry[UserJson]]
  when(mockedScentry.isAuthenticated(any[HttpServletRequest], any[HttpServletResponse])) thenReturn authenticated

  when(mockedScentry.authenticate(anyVararg[String]())(any[HttpServletRequest], any[HttpServletResponse])) thenReturn Some(newUser)

  override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = mockedScentry

  override def user(implicit request: javax.servlet.http.HttpServletRequest) = newUser

}
