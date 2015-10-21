package hclu.hreg.service

import java.security.{MessageDigest, SecureRandom}

object Crypto {
  val hex = "0123456789ABCDEF"
  val lineSeparator = System.getProperty("line.separator")

  lazy val random = SecureRandom.getInstance("SHA1PRNG")

  def sha1(text: String): String = sha1(unifyLineSeparator(text).getBytes("ASCII"))

  private def sha1(bytes: Array[Byte]): String = digest(bytes, MessageDigest.getInstance("SHA1")).take(7).toLowerCase

  def generateId: String = {
    val bytes = Array.fill(32)(0.byteValue)
    random.nextBytes(bytes)
    sha1(bytes)
  }

  def digest(bytes: Array[Byte], md: MessageDigest): String = {
    md.update(bytes)
    hexify(md.digest)
  }

  def hexify(bytes: Array[Byte]): String = {
    val builder = new java.lang.StringBuilder(bytes.length * 2)
    bytes.foreach { byte => builder.append(hex.charAt((byte & 0xF0) >> 4)).append(hex.charAt(byte & 0xF)) }
    builder.toString
  }

  private def unifyLineSeparator(text: String): String = text.replaceAll(lineSeparator, "\n")
}