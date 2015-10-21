package hclu.hreg.api

import hclu.hreg.common.StringJsonWrapper
import hclu.hreg.service.doc.DocService
import hclu.hreg.service.model.DocIdsJson
import hclu.hreg.service.user.UserService
import org.scalatra.swagger.{StringResponseMessage, Swagger, SwaggerSupport}
import org.scalatra.{AsyncResult, BadRequest, Created, FutureSupport}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

class DocsServlet(val docService: DocService, val userService: UserService)(override implicit val swagger: Swagger, ec: ExecutionContext)
    extends JsonServletWithAuthentication with SwaggerMappable with DocsServlet.ApiDocs with FutureSupport with ServletHelpers {

  override def mappingPath = DocsServlet.MappingPath

  override protected implicit def executor = ec

  post("/add", operation(add)) {
    haltIfNotAuthenticated()

    val preId = preIdOpt
    val postId = postIdOpt
    val senderDescription = senderDescriptionOpt
    val description = descriptionOpt
    val primaryRecipient = primaryRecipientOpt
    val secondaryRecipient = senderDescriptionOpt
    val note = noteOpt
    val scannedDocumentId = scannedDocumentIdOpt

    val emailDocumentId = emailDocumentIdOpt
    val emailDocumentName = emailDocumentNameOpt

    val scannedDocumentNameV = scannedDocumentNameOpt.toSuccess("scannedDocumentName")
    val scannedDocumentIdV = scannedDocumentId.toSuccess("scannedDocumentId")

    val res = for {
      scannedDocumentId <- scannedDocumentIdV
      scannedDocumentName <- scannedDocumentNameV
    } yield new AsyncResult {
      val is = for {
        preDoc <- docByRegId(preId)
        postDoc <- docByRegId(postId)
        ids <- docService.addDocument(
          user.id,
          preDoc.map(_.id),
          postDoc.map(_.id),
          senderDescription,
          description,
          primaryRecipient,
          secondaryRecipient,
          scannedDocumentId,
          scannedDocumentName,
          emailDocumentId,
          emailDocumentName,
          note
        )
      } yield Created(DocIdsJson(ids._1, ids._2))

    }

    result(res.leftMap(missingField))

  }

  private def docByRegId(idOpt: Option[Int]) = idOpt match {
    case Some(id) => docService.findByRegId(id)
    case _ => Future.successful(None)
  }

  private def preIdOpt: Option[Int] = parse[Int]("preId")

  private def postIdOpt: Option[Int] = parse[Int]("postId")

  private def senderDescriptionOpt: Option[String] = parse[String]("senderDescription")

  private def descriptionOpt: Option[String] = parse[String]("description")

  private def primaryRecipientOpt: Option[String] = parse[String]("primaryRecipient")

  private def secondaryRecipientOpt: Option[String] = parse[String]("secondaryRecipient")

  private def scannedDocumentIdOpt: Option[String] = parse[String]("scannedDocumentId")

  private def scannedDocumentNameOpt: Option[String] = parse[String]("scannedDocumentName")

  private def emailDocumentIdOpt: Option[String] = parse[String]("emailDocumentId")

  private def emailDocumentNameOpt: Option[String] = parse[String]("emailDocumentName")

  private def noteOpt: Option[String] = parse[String]("note")

  def parse[A](fieldName: String)(implicit mf: scala.reflect.Manifest[A]): Option[A] = {
    (parsedBody \ fieldName).extractOpt[A]
  }

}

object DocsServlet {
  val MappingPath = "docs"

  // only enclosing object's companions have access to this trait
  protected trait ApiDocs extends SwaggerSupport {
    self: DocsServlet =>

    override protected val applicationDescription = "Document management"

    protected val add = (
      apiOperation[StringJsonWrapper]("add")
      summary "Add new document"
      parameter bodyParam[AddDocumentCommand]("body").description("Document data").required
      responseMessages (
        StringResponseMessage(201, "Created"),
        StringResponseMessage(401, "User not logged in")
      )
    )
  }

  private[this] case class AddDocumentCommand(
    preId: Int,
    postId: Int,
    senderDescription: String,
    description: String,
    primaryRecipient: String,
    secondaryRecipient: String,
    url: String,
    note: String
  )

}
