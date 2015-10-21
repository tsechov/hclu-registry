package hclu.hreg.dao

import java.util.UUID
import hclu.hreg.common.FutureHelpers._
import hclu.hreg.common.Pagination
import hclu.hreg.dao.sql.SqlDatabase
import hclu.hreg.domain.Contact
import org.joda.time.DateTime

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class ContactDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlContactSchema {
  def findAll = findAllActive

  import database._
  import database.driver.api._

  type ContactId = UUID

  def add(contact: Contact): Future[Unit] = {
    db.run(addAction(contact)).mapToUnit
  }

  private def addAction(contact: Contact) = contacts += contact

  def findById(id: UUID) = findOneWhere(_.id === id)

  def findById(id: String) = db.run(findOneWhereAction(_.id === UUID.fromString(id)))

  private def findOneWhere(condition: Contacts => Rep[Boolean]) = db.run(findOneWhereAction(condition))

  private def findOneWhereAction(condition: Contacts => Rep[Boolean]) = contacts.filter(condition).result.headOption

  private def findAllActive = db.run(findWhereAction(_.active === true))

  private def findWhereAction(condition: Contacts => Rep[Boolean], pagination: Option[Pagination] = None) = pagination match {
    case Some(page) => {
      val offset = (page.pageNumber - 1) * page.pageSize
      contacts.filter(condition).drop(offset).take(page.pageSize).result
    }
    case _ => contacts.filter(condition).result
  }

}

trait SqlContactSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val contacts = TableQuery[Contacts]

  protected class Contacts(tag: Tag) extends Table[Contact](tag, "CONTACTS") {
    // format: OFF
    def id = column[UUID]("ID", O.PrimaryKey)

    def firstname = column[Option[String]]("FIRSTNAME")

    def lastname = column[Option[String]]("LASTNAME")

    def email = column[String]("EMAIL")

    def active = column[Boolean]("ACTIVE")

    def * = (
      id,
      firstname,
      lastname,
      email,
      active) <>
      ((Contact.apply _).tupled, Contact.unapply)

    // format: ON
  }

}
