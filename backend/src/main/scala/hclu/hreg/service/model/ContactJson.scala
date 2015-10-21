package hclu.hreg.service.model

import java.util.UUID

import hclu.hreg.domain.Contact

case class ContactJson(id: UUID, firstname: Option[String], lastname: Option[String], email: String)

object ContactJson {
  def apply(contact: Contact) = new ContactJson(contact.id, contact.firstname, contact.lastname, contact.email)

  def apply(list: List[Contact]): List[ContactJson] = {
    for (contact <- list) yield ContactJson(contact)
  }

  def apply(contactOpt: Option[Contact]): Option[ContactJson] = contactOpt.map(ContactJson(_))
}

