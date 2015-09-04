package hclu.hreg.service.dropbox

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory.parseString
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
@Ignore
class DropboxServiceSpec extends TestKit(ActorSystem("DropboxServiceSpec", parseString(DropboxServiceSpec.config).resolve()))
    with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {

  override def afterAll {
    shutdown()
  }

  "DropboxService" should {
    "put files to dropbox" in {
      implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
      val target = new DropboxService(system, system.settings.config)

      import java.io._
      val tmp = File.createTempFile("DropboxServiceSpec", ".tmp")

      val pw = new PrintWriter(tmp)
      pw.write("Hello, dropbox")
      pw.close

      //val res = target.storeFile(FileToDropbox(tmp.getAbsolutePath, UUID.randomUUID()))

      val docId: Int = 12
      val res = for {
        remoteFile <- target.storeFile(docId)(new FileToDropbox(tmp.getAbsolutePath))
        link <- target.getLink(remoteFile.remoteName)
      } yield link

      whenReady(res) { res => println(s"remote link: $res") }

    }
  }
}

object DropboxServiceSpec {
  val config = """
    akka {
      loglevel = "WARNING"
    }

               """
}

