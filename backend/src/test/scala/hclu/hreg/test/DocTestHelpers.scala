package hclu.hreg.test

import java.util.UUID

import hclu.hreg.domain.Doc
import org.joda.time.DateTime

trait DocTestHelpers {
  def newDoc(id: UUID, createdOn: DateTime) = {

    val createdBy = UUID.randomUUID();
    val url = "urljool"
    Doc(id, -1, None, None, createdOn, createdBy, None, None, None, None, url, None, None, false, None, None, false)
  }
}
