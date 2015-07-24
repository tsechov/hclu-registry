package hclu.hreg.service.email

import hclu.hreg.service.templates.EmailContentWithSubject

import scala.concurrent.Future

trait EmailService {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject): Future[Unit]

}
