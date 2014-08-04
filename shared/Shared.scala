package shared

import scala.annotation.ClassfileAnnotation
import scala.scalajs.js.annotation.JSExport

class Web extends ClassfileAnnotation

@JSExport
case class MyCaseClass(oo: String)


@Web
trait Api {
  def hello(a: Int): Int
  def caseClass(cc: MyCaseClass): String
}