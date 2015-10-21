package hclu.hreg.api

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import hclu.hreg.BaseServletSpec
import hclu.hreg.dao.{ContactDao, UserDao}
import hclu.hreg.domain.Contact
import hclu.hreg.service.contact.ContactService
import hclu.hreg.service.email.DummyEmailService
import hclu.hreg.service.templates.EmailTemplatingEngine
import hclu.hreg.service.user.{RegistrationDataValidator, UserService}
import hclu.hreg.test.{FlatSpecWithSql}
import org.json4s.JsonDSL._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class ContactsServletSpec extends BaseServletSpec with FlatSpecWithSql {
  var servlet: ContactsServlet = _

  def onServletWithMocks(testToExecute: (ContactService) => Unit) = {
    val dao = new ContactDao(sqlDatabase)
    val userDao = new UserDao(sqlDatabase)

    dao.add(Contact(UUID.randomUUID(), Some("first1"), Some("last1"), "contact1@sml.com"))
    dao.add(Contact(UUID.randomUUID(), Some("first2"), Some("last2"), "contact2@sml.com"))

    val userService = spy(new UserService(userDao, new RegistrationDataValidator(), new DummyEmailService(), new EmailTemplatingEngine))
    val contactService = new ContactService(dao)
    servlet = new ContactsServlet(contactService, userService) {
      override protected def isAuthenticated(implicit request: HttpServletRequest): Boolean = true
    }
    addServlet(servlet, "/*")

    testToExecute(contactService)
  }

  "POST /" should "register new contact" in {
    onServletWithMocks {
      (contactService) =>
        post("/", mapToJson(Map("firstname" -> "firstname", "lastname" -> "lastname", "email" -> "newUser@sml.com")), defaultJsonHeaders) {
          status should be (201)
        }
    }
  }

  "POST /" should "reject with invalid email" in {
    onServletWithMocks {
      (contactService) =>
        post("/", mapToJson(Map("firstname" -> "firstname", "lastname" -> "lastname", "email" -> "newUser")), defaultJsonHeaders) {
          val field: Option[String] = (stringToJson(body) \ "validationErrors" \ "field").extractOpt[String]
          val msg: Option[String] = (stringToJson(body) \ "validationErrors" \ "msg").extractOpt[String]
          status should be (400)

          field should be (Some("email"))
          msg should be (Some("invalid email format"))

        }
    }
  }
  "POST /" should "reject with no email" in {
    onServletWithMocks {
      (contactService) =>
        post("/", mapToJson(Map("firstname" -> "firstname", "lastname" -> "lastname")), defaultJsonHeaders) {
          val field: Option[String] = (stringToJson(body) \ "validationErrors" \ "field").extractOpt[String]
          val msg: Option[String] = (stringToJson(body) \ "validationErrors" \ "msg").extractOpt[String]
          status should be (400)

          field should be (Some("email"))
          msg should be (Some("no email provided"))

        }
    }
  }

  "GET /" should "fetch all the contacts" in {
    onServletWithMocks {
      (contactService) =>
        get("/") {
          status should be(200)
          body should include(""""firstname":"first1","lastname":"last1","email":"contact1@sml.com"}""")
          body should include(""""firstname":"first2","lastname":"last2","email":"contact2@sml.com"}""")
        }
    }
  }

  "GET /:id" should "fetch all contact by id" in {
    onServletWithMocks {
      (contactService) =>
        val expected = contactService.findAll.futureValue.head

        get(s"/${expected.id}") {
          status should be(200)
          body should include(""""firstname":"first1","lastname":"last1","email":"contact1@sml.com"}""")
        }
    }
  }

}
