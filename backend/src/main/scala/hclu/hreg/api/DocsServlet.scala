package hclu.hreg.api

import hclu.hreg.common.StringJsonWrapper
import hclu.hreg.service.doc.DocService
import hclu.hreg.service.model.DocIdsJson
import hclu.hreg.service.user.UserService
import org.scalatra.swagger.{StringResponseMessage, Swagger, SwaggerSupport}
import org.scalatra.{AsyncResult, Created, FutureSupport}

import scala.concurrent.{ExecutionContext, Future}

class DocsServlet(val docService: DocService, val userService: UserService)(override implicit val swagger: Swagger, ec: ExecutionContext)
    extends JsonServletWithAuthentication with SwaggerMappable with DocsServlet.ApiDocs with FutureSupport {

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
    val urlx = url
    val note = noteOpt

    new AsyncResult {
      val is = for {
        preDoc <- docByRegId(preId)
        postDoc <- docByRegId(postId)
        ids <- docService.addDocument(user.id, preDoc.map(_.id), postDoc.map(_.id), senderDescription, description, primaryRecipient, secondaryRecipient, urlx, note)
      } yield Created(DocIdsJson(ids._1, ids._2))

    }
  }

  private def docByRegId(idOpt: Option[Int]) = idOpt match {
    case Some(id) => docService.findByRegId(id)
    case _ => Future.successful(None)
  }

  private def preIdOpt: Option[Int] = (parsedBody \ "preId").extractOpt[Int]

  private def postIdOpt: Option[Int] = (parsedBody \ "postId").extractOpt[Int]

  private def senderDescriptionOpt: Option[String] = (parsedBody \ "senderDescription").extractOpt[String]

  private def descriptionOpt: Option[String] = (parsedBody \ "description").extractOpt[String]

  private def primaryRecipientOpt: Option[String] = (parsedBody \ "primaryRecipient").extractOpt[String]

  private def secondaryRecipientOpt: Option[String] = (parsedBody \ "secondaryRecipient").extractOpt[String]

  private def url: String = (parsedBody \ "url").extract[String]

  private def noteOpt: Option[String] = (parsedBody \ "note").extractOpt[String]

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
