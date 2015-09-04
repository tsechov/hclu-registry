package hclu.hreg.test

import java.util.UUID

import hclu.hreg.domain.Doc
import org.joda.time.DateTime

trait DocTestHelpers {
  def newDoc(id: UUID, scanDocId: String, scanDocName: String, createdOn: DateTime) = {

    val createdBy = UUID.randomUUID();

    Doc(id, -1, None, None, createdOn, createdBy, None, None, None, None, scanDocId, scanDocName, None, None, None, None, false, None, None, false)
  }
}
