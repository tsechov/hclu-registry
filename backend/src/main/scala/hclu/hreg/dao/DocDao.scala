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

trait SqlDocSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val docs = TableQuery[Docs]

  protected class Docs(tag: Tag) extends Table[Doc](tag, "docs") {
    // format: OFF
    def id = column[UUID]("id", O.PrimaryKey)

    def regId = column[Int]("reg_id", O.AutoInc)

    def preId = column[Option[UUID]]("pre_id")

    def postId = column[Option[UUID]]("post_id")

    def createdOn = column[DateTime]("created_on")

    def createdBy = column[UUID]("created_by")

    def senderDescription = column[Option[String]]("sender_description")

    def description = column[Option[String]]("description")

    def primaryRecipient = column[Option[String]]("primary_recipient")

    def secondaryRecipient = column[Option[String]]("secondary_recipient")

    def scannedDocumentId = column[String]("scan_doc_id")

    def scannedDocumentName = column[String]("scan_doc_name")

    def emailDocumentId = column[Option[String]]("email_doc_id")

    def emailDocumentName = column[Option[String]]("email_doc_name")

    def emailId = column[Option[UUID]]("email_id")

    def note = column[Option[String]]("note")

    def saved = column[Boolean]("saved")

    def savedOn = column[Option[DateTime]]("saved_on")

    def savedBy = column[Option[UUID]]("saved_by")

    def deleted = column[Boolean]("deleted")

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

    // format: ON
  }

}
