package hclu.hreg.service.dropbox

import java.io.File
import java.util.Locale

import akka.actor.{Actor, Props}
import com.dropbox.core.{DbxClient, DbxRequestConfig}
import hclu.hreg.service.dropbox.Dropbox.{FileToStore, GetLink}
import org.apache.camel.component.dropbox.core.DropboxAPIFacade
import org.apache.camel.component.dropbox.util.DropboxUploadMode

class Dropbox(clientIdentifier: String, accessToken: String) extends Actor {

  val config: DbxRequestConfig = new DbxRequestConfig(clientIdentifier, Locale.getDefault.toString)
  val client: DbxClient = new DbxClient(config, accessToken)
  val facade = DropboxAPIFacade.getInstance(client)

  private def store(file: FileToStore): FileToStore = {
    facade.put(new File(file.localName).getAbsolutePath, s"/${file.remoteName}", DropboxUploadMode.force)
    file
  }

  private def getLink(remoteId: String): String = {
    client.createShareableUrl(s"/$remoteId")
  }

  def receive = {
    case file: FileToStore => sender ! store(file)
    case GetLink(remoteId) => sender ! getLink(remoteId)
  }

}

object Dropbox {
  def props(clientIdentifier: String, accessToken: String): Props = Props(new Dropbox(clientIdentifier, accessToken))
  case class FileToStore(localName: String, remoteName: String)
  case class GetLink(remoteId: String)
}

