package hclu.hreg.service

import java.io.File

trait TempFileSupport {
  val javaIOTmpdir = sys.props("java.io.tmpdir")

  def fromDocId(docId: String) = new File(javaIOTmpdir + File.separator + docId)

  def extension(fileName: String) = fileName.drop(fileName.lastIndexOf('.') + 1)
}
