package hclu.hreg.dao

import java.util.UUID

import hclu.hreg.dao.sql.SqlDatabase

case class DocRecipient(docId: UUID, recipientId: UUID)

trait SqlDocRecipientSchema extends SqlContactSchema {

  this: SqlDocSchema =>

  protected val database: SqlDatabase

  import database.driver.api._

  protected val DocRecipientQuery = TableQuery[DocRecipientTable]

  protected class DocRecipientTable(tag: Tag) extends Table[DocRecipient](tag, "DOCS_RECIPIENTS") {
    def docId = column[UUID]("DOC_ID")

    def recipientId = column[UUID]("RECIPIENT_ID")

    def * = (docId, recipientId) <> (DocRecipient.tupled, DocRecipient.unapply)

    def docFK = foreignKey("DOC_RECIPIENT_DOC_FK", docId, docs)(d => d.id)
    def recipientFK = foreignKey("DOC_RECIPIENT_CONTACT_FK", recipientId, contacts)(r => r.id)

  }

}

