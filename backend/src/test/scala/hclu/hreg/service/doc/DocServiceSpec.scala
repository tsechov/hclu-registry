package hclu.hreg.service.doc

import java.util.UUID

import hclu.hreg.common.{FixtureTimeClock, RealTimeClock}
import hclu.hreg.dao.DocDao
import hclu.hreg.domain.{Doc, User}
import hclu.hreg.service.TempFileSupport
import hclu.hreg.service.dropbox.Dropbox.FileToStore
import hclu.hreg.service.dropbox.{FileToDropbox, DropboxService}
import hclu.hreg.service.templates.EmailContentWithSubject

import hclu.hreg.test.{DocTestHelpers, FlatSpecWithSql}
import org.apache.commons.lang3.RandomStringUtils
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest
import org.scalatest.mock.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocServiceSpec extends FlatSpecWithSql with scalatest.Matchers with MockitoSugar with DocTestHelpers with TempFileSupport {
  def prepareDocDaoMock: DocDao = {
    val dao = new DocDao(sqlDatabase)
    Future.sequence(Seq(
      dao.add(newDoc(uuid, "foo", "bar", clock.now), { _ => Future.successful() }),
      dao.add(newDoc(uuid, "foo", "bar", clock.now), { _ => Future.successful() })
    )).futureValue
    dao
  }

  def prepareDropboxMock: DropboxService = {
    mock[DropboxService]
  }

  override implicit val clock = new FixtureTimeClock(System.currentTimeMillis())
  var dao: DocDao = _
  var target: DocService = _
  var dropbox: DropboxService = _

  override protected def beforeEach() = {
    super.beforeEach()
    dao = prepareDocDaoMock
    dropbox = prepareDropboxMock

    target = new DocService(dao, dropbox)
  }

  "addDoc" should "add doc with unique regid" in {
    val userId: UUID = uuid
    val preId: Option[UUID] = Some(uuid)
    val postId: Option[UUID] = Some(uuid)
    val senderDescription: Option[String] = Some(rndString)
    val description: Option[String] = Some(rndString)
    val primaryRecipient: Option[String] = Some(rndString)
    val secondaryRecipient: Option[String] = Some(rndString)
    val scannedDocumentId: String = rndString
    val scannedDocumentName: String = rndString
    val emailDocumentId: Option[String] = Some(rndString)
    val emailDocumentName: Option[String] = Some(rndString)
    val note: Option[String] = Some(rndString)

    when(dropbox.storeFile(any[Int])(any[FileToDropbox])).thenAnswer(new Answer[Future[FileToStore]]() {

      override def answer(invocation: InvocationOnMock): Future[FileToStore] = {
        val arg = invocation.getArgumentAt[FileToDropbox](1, classOf[FileToDropbox])
        Future.successful(FileToStore("foo", "bar"))
      }
    })

    // When
    val (id, regId) = target.addDocument(
      userId,
      preId,
      postId,
      senderDescription,
      description,
      primaryRecipient,
      secondaryRecipient,
      scannedDocumentId,
      scannedDocumentName,
      emailDocumentId,
      emailDocumentName,
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
    doc.scannedDocumentId should be (FileToDropbox(fromDocId(scannedDocumentId).getAbsolutePath).id)
    doc.scannedDocumentName should be (scannedDocumentName)
    doc.emailDocumentId should be (Some(FileToDropbox(fromDocId(emailDocumentId.get).getAbsolutePath).id))
    doc.emailDocumentName should be (emailDocumentName)
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
