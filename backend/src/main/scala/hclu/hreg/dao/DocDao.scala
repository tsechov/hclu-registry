package hclu.hreg.dao

import java.util.UUID

import hclu.hreg.dao.sql.SqlDatabase
import hclu.hreg.domain.Doc
import org.joda.time.DateTime

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class DocDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlDocSchema {

  import database._
  import database.driver.api._

  type DocId = UUID

  def add(doc: Doc, postAction: (Int) => Future[Unit]): Future[Int] = {
    val action = (for {
      regId <- addAction(doc)

    } yield {
      Await.result(postAction(regId), 20 seconds)
      regId
    }).transactionally

    db.run(action)
  }

  def findById(id: UUID) = findOneWhere(_.id === id)

  def findByRegId(regId: Int) = db.run(findByRegIdAction(regId))

  def findByRegIdAction(regId: Int) = findOneWhereAction(_.regId === regId)

  private def findOneWhere(condition: Docs => Rep[Boolean]) = {
    db.run(findOneWhereAction(condition))
  }

  private def findOneWhereAction(condition: Docs => Rep[Boolean]) = {
    docs.filter(condition).result.headOption
  }

  private def addAction(doc: Doc) = {
    (docs returning docs.map(_.regId)) += doc
  }

}

trait SqlDocSchema extends SqlDocRecipientSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val docs = TableQuery[Docs]

  protected class Docs(tag: Tag) extends Table[Doc](tag, "DOCS") {
    // format: OFF
    def id = column[UUID]("ID", O.PrimaryKey)

    def regId = column[Int]("REG_ID", O.AutoInc)

    def preId = column[Option[UUID]]("PRE_ID")

    def postId = column[Option[UUID]]("POST_ID")

    def createdOn = column[DateTime]("CREATED_ON")

    def createdBy = column[UUID]("CREATED_BY")

    def senderDescription = column[Option[String]]("SENDER_DESCRIPTION")

    def description = column[Option[String]]("DESCRIPTION")

    def primaryRecipient = column[Option[String]]("PRIMARY_RECIPIENT")

    def secondaryRecipient = column[Option[String]]("SECONDARY_RECIPIENT")

    def scannedDocumentId = column[String]("SCAN_DOC_ID")

    def scannedDocumentName = column[String]("SCAN_DOC_NAME")

    def emailDocumentId = column[Option[String]]("EMAIL_DOC_ID")

    def emailDocumentName = column[Option[String]]("EMAIL_DOC_NAME")

    def emailId = column[Option[UUID]]("EMAIL_ID")

    def note = column[Option[String]]("NOTE")

    def saved = column[Boolean]("SAVED")

    def savedOn = column[Option[DateTime]]("SAVED_ON")

    def savedBy = column[Option[UUID]]("SAVED_BY")

    def deleted = column[Boolean]("DELETED")

    def * = (
      id,
      regId,
      preId,
      postId,
      createdOn,
      createdBy,
      senderDescription,
      description,
      primaryRecipient,
      secondaryRecipient,
      scannedDocumentId,
      scannedDocumentName,
      emailDocumentId,
      emailDocumentName,
      emailId,
      note,
      saved,
      savedOn,
      savedBy,
      deleted) <>
      ((Doc.apply _).tupled, Doc.unapply)

    def recipients = DocRecipientQuery.filter(_.docId === id)
      .flatMap(_.recipientFK)

    // format: ON
  }

}
