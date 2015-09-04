package hclu.hreg.api

import java.io.{File, FileOutputStream, FileWriter, IOException}

import hclu.hreg.service.{Crypto, TempFileSupport}
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}

case class UploadResponse(name: String, id: String)

class UploadServlet extends JsonServlet with FileUploadSupport with Mappable with TempFileSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

  override def mappingPath: String = "upload"

  post("/") {
    fileParams.get("file") match {
      case Some(file) => {
        try {
          val ext = extension(file.getName)
          val outFile = fromDocId(Crypto.generateId + "." + ext)
          using(new FileOutputStream(outFile))(fos => fos.write(file.get))
          outFile.deleteOnExit()
          Ok(UploadResponse(file.getName, outFile.getName))
        }
        catch {
          case e: IOException => throw new RuntimeException(e)
        }
      }

      case None => BadRequest()
    }
  }

  def writeStringToFile(file: File, data: String, appending: Boolean = false) = using(new FileWriter(file, appending))(_.write(data))

  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = try f(resource) finally resource.close()

}
