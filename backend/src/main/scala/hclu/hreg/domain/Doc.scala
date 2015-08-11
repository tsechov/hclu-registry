package hclu.hreg.domain

import java.util.UUID

import org.joda.time.DateTime

case class Doc(
                id: UUID,
                regId: Int,
                preId: UUID,
                postId: UUID,
                createdOn: DateTime,
                createdBy: UUID,
                senderDescription: String,
                description: String,
                primaryRecipient: String,
                secondaryRecipient: String,
                url: String,
                emailId: UUID,
                note: String,
                saved: Boolean,
                savedOn: DateTime,
                savedBy: UUID,
                deleted: Boolean
                ) {

}
