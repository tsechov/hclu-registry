package hclu.hreg.service.doc

import java.util.UUID

import hclu.hreg.common.Clock
import hclu.hreg.dao.DocDao
import hclu.hreg.domain.Doc

import scala.concurrent.{ExecutionContext, Future}

class DocService(docDao: DocDao)(implicit ec: ExecutionContext, clock: Clock) {
  def addDocument(
    userId: UUID,
    preId: Option[UUID],
    postId: Option[UUID],
    senderDescription: Option[String],
    description: Option[String],
    primaryRecipient: Option[String],
    secondaryRecipient: Option[String],
    url: String,
    note: Option[String]
  ): Future[(UUID, Int)] = {

    val id = UUID.randomUUID()
    val createdOn = clock.nowUtc

    val doc = Doc(id, -1, preId, postId, createdOn, userId, senderDescription, description, primaryRecipient, secondaryRecipient, url, None, note, false, None, None, false)

    docDao.add(doc).map((id, _))

  }
}
