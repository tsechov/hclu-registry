package hclu.hreg.service.doc

import java.util.UUID

import hclu.hreg.common.{FixtureTimeClock, RealTimeClock}
import hclu.hreg.dao.DocDao
import hclu.hreg.domain.{Doc, User}
import hclu.hreg.service.templates.EmailContentWithSubject

import hclu.hreg.test.{DocTestHelpers, FlatSpecWithSql}
import org.apache.commons.lang3.RandomStringUtils
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest
import org.scalatest.mock.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocServiceSpec extends FlatSpecWithSql with scalatest.Matchers with MockitoSugar with DocTestHelpers {
  def prepareDocDaoMock: DocDao = {
    val dao = new DocDao(sqlDatabase)
    Future.sequence(Seq(
      dao.add(newDoc(uuid, clock.now)),
      dao.add(newDoc(uuid, clock.now))
    )).futureValue
    dao
  }
  override implicit val clock = new FixtureTimeClock(System.currentTimeMillis())
  var dao: DocDao = _
  var target: DocService = _

  override protected def beforeEach() = {
    super.beforeEach()
    dao = prepareDocDaoMock

    target = new DocService(dao)
  }

  "addDoc" should "add doc with unique regid" in {
    val userId: UUID = uuid
    val preId: Option[UUID] = Some(uuid)
    val postId: Option[UUID] = Some(uuid)
    val senderDescription: Option[String] = Some(rndString)
    val description: Option[String] = Some(rndString)
    val primaryRecipient: Option[String] = Some(rndString)
    val secondaryRecipient: Option[String] = Some(rndString)
    val url: String = rndString
    val note: Option[String] = Some(rndString)

    // When
    val (id, regId) = target.addDocument(
      userId,
      preId,
      postId,
      senderDescription,
      description,
      primaryRecipient,
      secondaryRecipient,
      url,
      note
    ).futureValue

    // Then
    val docOpt: Option[Doc] = dao.findByRegId(regId).futureValue
    docOpt.isDefined should be (true)
    val doc = docOpt.get

    doc.id should be (id)
    doc.preId should be (preId)
    doc.postId should be (postId)
    doc.senderDescription should be (senderDescription)
    doc.description should be (description)
    doc.primaryRecipient should be (primaryRecipient)
    doc.secondaryRecipient should be (secondaryRecipient)
    doc.url should be (url)
    doc.note should be (note)

    doc.createdOn should be (clock.nowUtc)
    doc.createdBy should be (userId)

    doc.saved should be (false)
    doc.savedBy should be (None)
    doc.savedOn should be (None)
    doc.deleted should be (false)

  }

  private def uuid: UUID = UUID.randomUUID()
  private def rndString = RandomStringUtils.randomAlphanumeric(12)
}
