package hclu.hreg.api

import hclu.hreg.common.StringJsonWrapper
import hclu.hreg.service.contact.ContactService
import hclu.hreg.service.model.ContactJson
import hclu.hreg.service.user.UserService
import org.scalatra.swagger.{StringResponseMessage, Swagger, SwaggerSupport}
import org.scalatra.{AsyncResult, Created, FutureSupport}

import scala.concurrent.ExecutionContext
import scalaz.Monoid
import scalaz.Scalaz._

class ContactsServlet(val service: ContactService, val userService: UserService)(override implicit val swagger: Swagger, ec: ExecutionContext)
    extends JsonServletWithAuthentication with SwaggerMappable with ContactsServlet.ApiDocs with FutureSupport with ServletHelpers {

  override def mappingPath = ContactsServlet.MappingPath

  override protected implicit def executor = ec

  implicit val invalidEmail: Monoid[ValidationError] = new Monoid[ValidationError] {
    override def zero: ValidationError = ValidationError("email", "invalid email format")

    override def append(f1: ValidationError, f2: => ValidationError): ValidationError = throw new UnsupportedOperationException("shouldnt get here")
  }

  post("/", operation(add)) {
    haltIfNotAuthenticated()

    val firstName = firstnameOpt
    val lastName = lastnameOpt

    val emailV = emailOpt.toSuccess("email").leftMap(missingField).filter(check)

    val res = for {
      email <- emailV
    } yield new AsyncResult {
      val is = for {
        id <- service.addContact(firstName, lastName, email)
      } yield Created(Map("id" -> id))
    }

    result(res)

  }

  get("/", operation(list)) {
    haltIfNotAuthenticated()
    service.findAll
  }

  get("/:id", operation(getById)) {
    haltIfNotAuthenticated()
    val id = params("id")
    service.findById(id)
  }

  def firstnameOpt: Option[String] = parse[String]("firstname")

  def lastnameOpt: Option[String] = parse[String]("lastname")

  def emailOpt: Option[String] = parse[String]("email")

  def parse[A](fieldName: String)(implicit mf: scala.reflect.Manifest[A]): Option[A] = {
    (parsedBody \ fieldName).extractOpt[A]
  }

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def check(e: String): Boolean = e match {
    case null => false
    case e if e.trim.isEmpty => false
    case e if emailRegex.findFirstMatchIn(e).isDefined => true
    case _ => false
  }
}

object ContactsServlet {
  val MappingPath = "contacts"

  // only enclosing object's companions have access to this trait
  protected trait ApiDocs extends SwaggerSupport {
    self: ContactsServlet =>

    override protected val applicationDescription = "Contact management"

    protected val add = (
      apiOperation[StringJsonWrapper]("add")
      summary "Add new contact"
      parameter bodyParam[AddContactCommand]("body").description("Contact data").required
      responseMessages (
        StringResponseMessage(201, "Created"),
        StringResponseMessage(401, "User not logged in")
      )
    )

    protected val list = (
      apiOperation[List[ContactJson]]("list")
      summary "Lists all contacts"
      responseMessages (
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "User not logged in")
      )
    )

    protected val getById = (
      apiOperation[Unit]("getById")
      summary "get contact by id"
      parameters (
        pathParam[String]("id").description("contact id").required
      )
        responseMessages (
          StringResponseMessage(200, "OK"),
          StringResponseMessage(404, "Not found")
        )
    )
  }

  private[this] case class AddContactCommand(
    firstname: String,
    lastname: String,
    email: String
  )

}
