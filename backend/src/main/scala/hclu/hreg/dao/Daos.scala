package hclu.hreg.dao

import hclu.hreg.dao.sql.SqlDatabase

import scala.concurrent.ExecutionContext

trait Daos {
  implicit val ec: ExecutionContext

  lazy val userDao = new UserDao(sqlDatabase)

  lazy val codeDao = new PasswordResetCodeDao(sqlDatabase)

  lazy val docDao = new DocDao(sqlDatabase)

  def sqlDatabase: SqlDatabase
}
