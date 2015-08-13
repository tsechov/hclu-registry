package hclu.hreg.api

import java.util.{Date, UUID}
import javax.servlet.http.HttpServletResponse

import com.typesafe.scalalogging.LazyLogging
import hclu.hreg.api.serializers.{DateTimeSerializer, RequestLogger, UuidSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json.{JValueResult, NativeJsonSupport}
import org.scalatra.swagger.SwaggerSupport

trait Mappable {

  val Prefix = "/api/"

  def fullMappingPath = Prefix + mappingPath

  def mappingPath: String
}

trait SwaggerMappable {
  self: Mappable with SwaggerSupport =>

  def name = Prefix.tail + mappingPath
}

abstract class JsonServlet extends ScalatraServlet with RequestLogger with NativeJsonSupport with JValueResult with LazyLogging with Halting with Mappable {

  protected implicit val jsonFormats: Formats = DefaultFormats + new DateTimeSerializer() + new UuidSerializer()

  val Expire = new Date().toString

  before() {
    contentType = formats("json")
    applyNoCache(response)
  }

  def applyNoCache(response: HttpServletResponse) {
    response.addHeader("Expires", Expire)
    response.addHeader("Last-Modified", Expire)
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
    response.addHeader("Pragma", "no-cache")
  }

  case class ErrorResponse(msg: String, id: UUID = UUID.randomUUID()) {
    def xHeader = Map("X-Hreg-Error-Id" -> id.toString)
  }

  errorHandler = {
    case t: org.json4s.MappingException => {
      val error = ErrorResponse("Request parsing error")
      logger.info(error.toString, t)
      halt(BadRequest(error, error.xHeader))
    }
    case t: Exception => {
      val error = ErrorResponse("Request processing error")
      logger.error(error.toString, t)
      halt(InternalServerError(error, error.xHeader))
    }
  }

}