import scala.quoted._

object Macro {
  inline def impl: String = ${doImpl}

  private def doImpl(using Quotes): Expr[String] = {
    MacroVis.symbolInstance
    ???
  }
}

object MacroVis:
  given symbolInstance: String = summon[String]
