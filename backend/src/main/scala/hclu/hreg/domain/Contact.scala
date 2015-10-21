package hclu.hreg.domain

import java.util.UUID

case class Contact(
  id: UUID,
  firstname: Option[String],
  lastname: Option[String],
  email: String,
  active: Boolean = true
)