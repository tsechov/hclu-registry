package hclu.hreg.api

import hclu.hreg.version.BuildInfo._
import org.scalatra.swagger.{StringResponseMessage, Swagger, SwaggerSupport}

case class VersionJson(
  build: String,
  date: String,
  version: String,
  branch: String,
  buildNumber: String,
  buildUrl: String
)

class VersionServlet(override implicit val swagger: Swagger) extends JsonServlet with SwaggerMappable with VersionServlet.ApiDocs {

  override def mappingPath = VersionServlet.MappingPath

  get("/", operation(getVersion)) {
    VersionJson(buildSha.take(7), buildDate, version, branch, buildNumber, buildUrl)
  }
}

object VersionServlet {
  val MappingPath = "version"

  // only enclosing object's companions have access to this trait
  protected trait ApiDocs extends SwaggerSupport {
    self: VersionServlet =>

    override protected val applicationDescription = "Application version information"

    protected val getVersion = (
      apiOperation[VersionJson]("getVersion")
      summary "Get version"
      responseMessages (
        StringResponseMessage(200, "OK")
      )
    )
  }

}
