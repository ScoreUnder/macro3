import scala.quoted._

object Macro {
  inline def impl: String = ${doImpl}

  private def doImpl(using Quotes): Expr[String] = {
    import quotes.reflect.Symbol
    val mv = new MacroVis(using quotes)
    mv.symbolInstance.vis(Symbol.spliceOwner.owner.owner)
    Expr(Symbol.spliceOwner.owner.owner.tree.toString)
  }
}
