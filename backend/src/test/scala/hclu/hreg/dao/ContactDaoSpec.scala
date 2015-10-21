package hclu.hreg.dao

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import hclu.hreg.domain.Contact
import hclu.hreg.test.FlatSpecWithSql
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class ContactDaoSpec extends FlatSpecWithSql with LazyLogging with Matchers {
  behavior of "ContactDao"

  val createdOn = new DateTime(2015, 6, 3, 13, 25, 3, DateTimeZone.UTC)
  var dao: ContactDao = new ContactDao(sqlDatabase)

  def generateRandomId = UUID.randomUUID()

  lazy val randomIds: List[UUID] = List.fill(3)(generateRandomId)

  override def beforeAll() {
    super.beforeAll()

    for (i <- 1 to randomIds.size) {
      val id = randomIds(i - 1)
      val contact = Contact(id, Some("first"), Some("last"), s"email${id}")
      dao.add(contact).futureValue
    }
  }

  it should "add new contact" in {
    // Given
    val id = UUID.randomUUID()

    // When
    dao.add(Contact(id, Some("first"), Some("last"), "email")).futureValue //should be (4)

    // Then

    dao.findById(id).futureValue should be('defined)
  }

  it should "find contact by id" in {
    // Given

    randomIds foreach { id =>
      dao.findById(id).futureValue should be('defined)
    }

  }

}
