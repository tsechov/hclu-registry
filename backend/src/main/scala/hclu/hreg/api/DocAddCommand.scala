package hclu.hreg.api

import org.scalatra.validation.Validators.{PredicateValidator, Validator}

import org.scalatra.commands._

import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}

class DocAddCommand {

}

class TodosStringValidations(b: FieldDescriptor[String]) {

  // define a validation which we can apply to a [Field]
  def startsWithCap(message: String = "%s must start with a capital letter.") = b.validateWith(_ =>
    _ flatMap { new PredicateValidator[String](b.name, """^[A-Z,0-9]""".r.findFirstIn(_).isDefined, message).validate(_) })
}

abstract class TodosCommand[S](implicit mf: Manifest[S]) extends ModelCommand[S] with JsonCommand {

  /**
   * Decorate the [org.scalatra.commands.FieldDescriptor] class with our [TodosStringValidations]
   *
   * This adds the validation to the binding for the FieldDescriptor's b.validateWith function.
   */
  implicit def todoStringValidators(b: FieldDescriptor[String]) = new TodosStringValidations(b)
}
/** A command to validate and create Todo objects. */
class CreateTodoCommand extends TodosCommand[Todo] {

  // add json format handling so the command can do automatic conversions.
  protected implicit val jsonFormats = DefaultFormats

  // the validation conditions on the name field.
  val name: Field[String] = asType[String]("name").notBlank.minLength(3).startsWithCap()

}

case class Todo(id: Int, name: String, done: Boolean = false)