package hclu.hreg

import hclu.hreg.common.RealTimeClock
import hclu.hreg.common.logging.bugsnag.BugsnagErrorReporter
import hclu.hreg.dao.sql.SqlDatabase
import hclu.hreg.dao.{DatabaseConfig, Daos}
import hclu.hreg.service.PasswordRecoveryService
import hclu.hreg.service.config.{CoreConfig, EmailConfig}
import hclu.hreg.service.doc.DocService
import hclu.hreg.service.email.{DummyEmailService, SmtpEmailService}
import hclu.hreg.service.templates.EmailTemplatingEngine
import hclu.hreg.service.user.{RegistrationDataValidator, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait Beans extends LazyLogging with Daos {
  lazy val config = new CoreConfig with EmailConfig with DatabaseConfig {
    override def rootConfig = ConfigFactory.load()
  }

  override lazy val sqlDatabase = SqlDatabase.create(config)
  override implicit val ec: ExecutionContext = global

  lazy val emailService = if (config.emailEnabled) {
    new SmtpEmailService(config)
  }
  else {
    logger.info("Starting with fake email sending service. No emails will be sent.")
    new DummyEmailService
  }

  implicit lazy val clock = RealTimeClock
  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(
    userDao,
    new RegistrationDataValidator(),
    emailService,
    emailTemplatingEngine
  )

  lazy val passwordRecoveryService = new PasswordRecoveryService(
    userDao,
    codeDao,
    emailService,
    emailTemplatingEngine,
    config
  )

  lazy val docService = new DocService(docDao)

  lazy val errorReporter = BugsnagErrorReporter(config)

}
