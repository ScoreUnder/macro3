import scala.quoted._

object Macro {
  inline def impl: String = ${doImpl}

  private def doImpl(using Quotes): Expr[String] = {
    new MacroVis(using quotes).symbolInstance
    ???
  }
}

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  given symbolInstance: String = summon[String]
}
