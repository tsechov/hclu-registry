package hclu.hreg.service.doc

import java.util.UUID

import hclu.hreg.common.Clock
import hclu.hreg.dao.DocDao
import hclu.hreg.domain.Doc
import hclu.hreg.service.TempFileSupport
import hclu.hreg.service.dropbox.{DropboxService, FileToDropbox}

import scala.concurrent.{ExecutionContext, Future}

class DocService(docDao: DocDao, dropbox: DropboxService)(implicit ec: ExecutionContext, clock: Clock) extends TempFileSupport {

  def store(scannedDocumentFile: FileToDropbox, emailDocumentFile: Option[FileToDropbox]): (Int) => Future[Unit] = { docId =>
    {
      val store = dropbox.storeFile(docId) _
      val files = emailDocumentFile match {
        case Some(emailFile) =>

          for {
            scannedFuture <- store(scannedDocumentFile)
            emailFuture <- store(emailFile)
          } yield (scannedFuture, Some(emailFuture))

        case None => for {
          scannedFuture <- store(scannedDocumentFile)
        } yield (scannedFuture, None)
      }
      files.map(_ => ())
    }
  }

  def addDocument(
    userId: UUID,
    preId: Option[UUID],
    postId: Option[UUID],
    senderDescription: Option[String],
    description: Option[String],
    primaryRecipient: Option[String],
    secondaryRecipient: Option[String],
    scannedDocumentId: String,
    scannedDocumentName: String,
    emailDocumentId: Option[String],
    emailDocumentName: Option[String],
    note: Option[String]
  ): Future[(UUID, Int)] = {

    val id = UUID.randomUUID()
    val createdOn = clock.nowUtc

    val scannedDocumentFile = FileToDropbox(fromDocId(scannedDocumentId).getAbsolutePath)

    val emailDocumentFile = emailDocumentId map { docId =>
      FileToDropbox(fromDocId(docId).getAbsolutePath)
    }

    val doc = Doc(
      id,
      -1,
      preId,
      postId,
      createdOn,
      userId,
      senderDescription,
      description,
      primaryRecipient,
      secondaryRecipient,
      scannedDocumentFile.id,
      scannedDocumentName,
      emailDocumentFile.map(_.id),
      emailDocumentName,
      None,
      note,
      false,
      None,
      None,
      false
    )

    docDao.add(doc, store(scannedDocumentFile, emailDocumentFile)).map((id, _))

  }

  def findByRegId(regId: Int): Future[Option[Doc]] = {
    docDao.findByRegId(regId)
  }
}
