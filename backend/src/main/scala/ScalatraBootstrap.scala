import java.util.{Locale, UUID}
import javax.servlet.ServletContext

import akka.actor.ActorSystem
import hclu.hreg.Beans
import hclu.hreg.api._
import hclu.hreg.api.swagger.SwaggerServlet
import hclu.hreg.common.Utils
import hclu.hreg.common.logging.AsyncErrorReportingLogAppender
import hclu.hreg.dao.sql.SqlDatabase
import hclu.hreg.domain.User
import hclu.hreg.version.BuildInfo
import org.scalatra.{LifeCycle, ScalatraServlet}

import scala.concurrent.Future

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {

  override def init(context: ServletContext) {
    Locale.setDefault(Locale.US) // set default locale to prevent Scalatra from sending cookie expiration date in polish format :)

    // Initialize error reporting client.
    AsyncErrorReportingLogAppender(config, errorReporter).init()

    SqlDatabase.updateSchema(sqlDatabase.connectionString)

    def mountServlet(servlet: ScalatraServlet with Mappable) {
      servlet match {
        case s: SwaggerMappable => context.mount(s, s.fullMappingPath, s.name)
        case _ => context.mount(servlet, servlet.fullMappingPath)
      }
    }

    mountServlet(new UsersServlet(userService))
    mountServlet(new PasswordRecoveryServlet(passwordRecoveryService, userService))
    mountServlet(new DocsServlet(docService, userService))
    mountServlet(new VersionServlet)
    mountServlet(new SwaggerServlet)
    mountServlet(new UploadServlet)
    mountServlet(new ContactsServlet(contactService, userService))

    context.setAttribute("appObject", this)

    logger.info("\nStarted HREG [{}]\nwith DB: {}", BuildInfo, sqlDatabase)

    val salt = Utils.randomString(128)
    val token = UUID.randomUUID().toString
    val now = clock.nowUtc
    val userCreatation: Future[Unit] = userDao.add(User.withRandomUUID("admin", "admin@drain.io", "xxx", salt, token, now, "admin", "admin"))

    userCreatation.onSuccess {
      case _ => logger.info("Admin added")
    }

  }

  override def destroy(context: ServletContext) {
    sqlDatabase.close()
    system.shutdown()
  }
}
