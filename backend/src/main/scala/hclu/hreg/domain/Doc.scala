package hclu.hreg.domain

import java.util.UUID

import org.joda.time.DateTime

case class Doc(
  id: UUID,
  regId: Int,
  preId: Option[UUID],
  postId: Option[UUID],
  createdOn: DateTime,
  createdBy: UUID,
  senderDescription: Option[String],
  description: Option[String],
  primaryRecipient: Option[String],
  secondaryRecipient: Option[String],
  url: String,
  emailId: Option[UUID],
  note: Option[String],
  saved: Boolean,
  savedOn: Option[DateTime],
  savedBy: Option[UUID],
  deleted: Boolean
)

