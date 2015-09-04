package hclu.hreg.service.dropbox

import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.Config
import hclu.hreg.service.dropbox.Dropbox.{FileToStore, GetLink}
import hclu.hreg.service.{Crypto, TempFileSupport}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class FileToDropbox(fileName: String) extends TempFileSupport {
  val id = s"${Crypto.sha1(fileName)}.${extension(fileName)}"

  def remoteId(docId: Int) = FileToDropbox.composeId(docId, id)
}

object FileToDropbox {
  def composeId(docId: Int, id: String) = s"${docId}-${id}"
}

class DropboxService(system: ActorSystem, config: Config)(implicit ec: ExecutionContext) {

  val dc = config.getConfig("dropbox")
  val dropbox = system.actorOf(Dropbox.props(dc.getString("clientId"), dc.getString("accessToken")))
  implicit val timeout = Timeout(30 seconds)

  def storeFile(docId: Int)(file: FileToDropbox): Future[FileToStore] = {

    val response = dropbox ? FileToStore(file.fileName, file.remoteId(docId))

    response.map {
      case f: FileToStore => {
        println(s"dropbox upload result: $f")
        f
      }
    }
  }

  def getLink(remoteId: String): Future[String] = {
    val response = dropbox ? GetLink(remoteId)
    response.map(_.asInstanceOf[String])
  }

}

