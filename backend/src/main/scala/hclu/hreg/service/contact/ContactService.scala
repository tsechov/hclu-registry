package hclu.hreg.service.contact

import java.util.UUID

import hclu.hreg.dao.ContactDao
import hclu.hreg.domain.{User, Contact}
import hclu.hreg.service.model.{ContactJson, UserJson}

import scala.concurrent.{ExecutionContext, Future}

class ContactService(dao: ContactDao)(implicit ec: ExecutionContext) {
  def findAll = dao.findAll.map(toJson)

  def addContact(
    firstname: Option[String],
    lastname: Option[String],
    email: String
  ): Future[UUID] = {

    val id = UUID.randomUUID()
    val contact = Contact(
      id,
      firstname,
      lastname,
      email
    )

    dao.add(contact).map(_ => id)

  }

  def findById(id: String): Future[Option[ContactJson]] = dao.findById(id).map(toJson)

  private def toJson(contactOpt: Option[Contact]) = contactOpt.map(ContactJson(_))
  private def toJson(contacts: Seq[Contact]) = contacts.map(ContactJson(_))

}
