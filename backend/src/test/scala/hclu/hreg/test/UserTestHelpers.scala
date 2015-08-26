package hclu.hreg.test

import java.util.UUID

import hclu.hreg.domain.User
import hclu.hreg.service.model.UserJson
import org.joda.time.{DateTimeZone, DateTime}

trait UserTestHelpers {

  val uuidStr = "de305d54-75b4-431b-adb2-eb6b9e546014"
  val createdOn = new DateTime(2015, 6, 3, 13, 25, 3, DateTimeZone.UTC)

  val newUser = new UserJson(
    UUID.fromString(uuidStr),
    "Jas Kowalski", "kowalski@kowalski.net", "token1", createdOn
  )

  def newUser(login: String, email: String, pass: String, salt: String, token: String, firstname: String, lastname: String) =
    User.withRandomUUID(login, email, pass, salt, token, createdOn, firstname, lastname)

}
