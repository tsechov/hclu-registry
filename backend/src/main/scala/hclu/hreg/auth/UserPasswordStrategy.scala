
package hclu.hreg.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import hclu.hreg.service.user.UserService
import hclu.hreg.service.model.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.concurrent.Await
import AuthOps._

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[UserJson] {

  override def name: String = UserPassword.name

  override def isValid(implicit request: HttpServletRequest) = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    Await.result(userService.authenticate(login, password), SyncUserResolveTimeout)
  }

}

object UserPassword {

  val name = "UserPassword"

}
