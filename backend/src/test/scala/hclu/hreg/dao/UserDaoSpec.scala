package hclu.hreg.dao

import java.util.UUID

import hclu.hreg.domain.User
import hclu.hreg.test.{FlatSpecWithSql, UserTestHelpers}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class UserDaoSpec extends FlatSpecWithSql with LazyLogging with UserTestHelpers with Matchers {
  behavior of "UserDao"

  var dao: UserDao = new UserDao(sqlDatabase)
  def generateRandomId = UUID.randomUUID()

  lazy val randomIds: List[UUID] = List.fill(3)(generateRandomId)

  override def beforeEach() {
    super.beforeEach()
    for (i <- 1 to randomIds.size) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      dao.add(User(randomIds(i - 1), login, login.toLowerCase, i + "email@sml.com", password, salt, token,
        createdOn, s"first${i}", s"last${i}", true))
        .futureValue
    }
  }

  it should "add new user" in {
    // Given
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    dao.add(newUser(login, email, "pass", "salt", "token", "first", "last")).futureValue

    // Then
    dao.findByEmail(email).futureValue should be ('defined)
  }

  it should "fail with exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    dao.add(newUser(login, "somePrefix" + email, "somePass", "someSalt", "someToken", "someFirst", "someLast")).futureValue

    // When & then
    dao.add(newUser(login, email, "pass", "salt", "token", "first", "last")).failed.futureValue.getMessage should equal(
      "User with given e-mail or login already exists"
    )
  }

  it should "fail with exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    dao.add(newUser("somePrefixed" + login, email, "somePass", "someSalt", "someToken", "someFirst", "someLast")).futureValue

    // When & then
    dao.add(newUser(login, email, "pass", "salt", "token", "first", "last")).failed.futureValue.getMessage should equal(
      "User with given e-mail or login already exists"
    )
  }

  it should "find by email" in {
    // Given
    val email = "1email@sml.com"

    // When
    val userOpt = dao.findByEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email))
  }

  it should "find by uppercase email" in {
    // Given
    val email = "1email@sml.com".toUpperCase

    // When
    val userOpt = dao.findByEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find by login" in {
    // Given
    val login = "user1"

    // When
    val userOpt = dao.findByLowerCasedLogin(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login))
  }

  it should "find by uppercase login" in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = dao.findByLowerCasedLogin(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using login with findByLoginOrEmail" in {
    // Given
    val login = "user1"

    // When
    val userOpt = dao.findByLoginOrEmail(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using uppercase login with findByLoginOrEmail" in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = dao.findByLoginOrEmail(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using email with findByLoginOrEmail" in {
    // Given
    val email = "1email@sml.com"

    // When
    val userOpt = dao.findByLoginOrEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find using uppercase email with findByLoginOrEmail" in {
    // Given
    val email = "1email@sml.com".toUpperCase

    // When
    val userOpt = dao.findByLoginOrEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find by token" in {
    // Given
    val token = "token1"

    // When
    val userOpt = dao.findByToken(token).futureValue

    // Then
    userOpt.map(_.token) should equal(Some(token))
  }

  it should "change password" in {
    // Given
    val login = "user1"
    val password = User.encryptPassword("pass11", "salt1")
    val user = dao.findByLoginOrEmail(login).futureValue.get

    // When
    dao.changePassword(user.id, password).futureValue
    val postModifyUserOpt = dao.findByLoginOrEmail(login).futureValue
    val u = postModifyUserOpt.get

    // Then
    u should be (user.copy(password = password))
  }

  it should "change login" in {
    // Given
    val user = dao.findByLowerCasedLogin("user1")
    val u = user.futureValue.get
    val newLogin = "changedUser1"

    // When
    dao.changeLogin(u.login, newLogin).futureValue
    val postModifyUser = dao.findByLowerCasedLogin(newLogin).futureValue

    // Then
    postModifyUser should equal(Some(u.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase)))
  }

  it should "change email" in {
    // Given
    val newEmail = "newmail@sml.pl"
    val user = dao.findByEmail("1email@sml.com").futureValue
    val u = user.get

    // When
    dao.changeEmail(u.email, newEmail).futureValue

    // Then
    dao.findByEmail(newEmail).futureValue should equal(Some(u.copy(email = newEmail)))
  }

}
