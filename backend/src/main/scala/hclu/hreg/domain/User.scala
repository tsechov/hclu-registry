package hclu.hreg.domain

import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import hclu.hreg.common.Utils
import org.joda.time.DateTime

case class User(
  id: UUID,
  login: String,
  loginLowerCased: String,
  email: String,
  password: String,
  salt: String,
  token: String,
  createdOn: DateTime,
  firstname: String,
  lastname: String,
  active: Boolean
)

object User {

  def withRandomUUID(
    login: String,
    email: String,
    plainPassword: String,
    salt: String,
    token: String,
    createdOn: DateTime,
    firstname: String,
    lastname: String,
    active: Boolean = true
  ): User =
    User(UUID.randomUUID(), login, login.toLowerCase, email, encryptPassword(plainPassword, salt), salt, token,
      createdOn, firstname, lastname, active)

  def encryptPassword(password: String, salt: String): String = {
    // 10k iterations takes about 10ms to encrypt a password on a 2013 MacBook
    val keySpec = new PBEKeySpec(password.toCharArray, salt.getBytes, 10000, 128)
    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val bytes = secretKeyFactory.generateSecret(keySpec).getEncoded
    Utils.toHex(bytes)
  }

  def passwordsMatch(plainPassword: String, user: User) = {
    user.password.equals(encryptPassword(plainPassword, user.salt))
  }
}
