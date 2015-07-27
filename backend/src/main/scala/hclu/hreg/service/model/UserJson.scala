package hclu.hreg.service.model

import java.util.UUID

import hclu.hreg.domain.User
import org.joda.time.DateTime

case class UserJson(id: UUID, login: String, email: String, token: String, createdOn: DateTime)

object UserJson {
  def apply(user: User) = new UserJson(user.id, user.login, user.email, user.token, user.createdOn)

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }

  def apply(userOpt: Option[User]): Option[UserJson] = userOpt.map(UserJson(_))
}
