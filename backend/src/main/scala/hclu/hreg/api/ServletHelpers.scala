package hclu.hreg.api

import org.scalatra.{AsyncResult, BadRequest}

import scalaz.{Failure, Success, Validation}

case class ValidationError(field: String, msg: String)

case class InvalidRequest(validationErrors: List[ValidationError])

trait ServletHelpers {
  //ValidationError(msg, s"no ${msg} provided")

  def result(v: Validation[ValidationError, AsyncResult]): Object = {
    v match {
      case Success(result) => result
      case Failure(err: ValidationError) => BadRequest(InvalidRequest(List(err)))
    }
  }

  val missingField: (String) => ValidationError = { msg => ValidationError(msg, s"no ${msg} provided") }
  val invalidFormat: (String) => ValidationError = { msg => ValidationError(msg, s"invalid ${msg} format") }

}
