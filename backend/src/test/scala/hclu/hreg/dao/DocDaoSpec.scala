package hclu.hreg.dao

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import hclu.hreg.domain.Doc
import hclu.hreg.test.{DocTestHelpers, FlatSpecWithSql}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class DocDaoSpec extends FlatSpecWithSql with LazyLogging with Matchers with DocTestHelpers {
  behavior of "DocDao"

  val createdOn = new DateTime(2015, 6, 3, 13, 25, 3, DateTimeZone.UTC)
  var dao: DocDao = new DocDao(sqlDatabase)

  def generateRandomId = UUID.randomUUID()

  lazy val randomIds: List[UUID] = List.fill(3)(generateRandomId)

  override def beforeEach() {
    super.beforeEach()

    for (i <- 1 to randomIds.size) {
      dao.add(newDoc(randomIds(i - 1), createdOn)).futureValue
    }
  }

  it should "add new doc" in {
    // Given
    val id = UUID.randomUUID()

    // When
    val newRegId = dao.add(newDoc(id, createdOn)).futureValue //should be (4)

    // Then
    newRegId should be(4)
    dao.findById(id).futureValue should be('defined)
  }

  it should "find doc by regId" in {
    // Given
    val ids = 1 to randomIds.size

    ids foreach { id =>
      dao.findByRegId(id).futureValue should be('defined)
    }

  }

}
