import scala.quoted._

object Macro {
  inline def impl: String = ${doImpl}

  private def doImpl(using Quotes): Expr[String] = {
    import quotes.reflect.Symbol
    val mv = new MacroVis(using quotes)
    import mv._
    mv.vis[Symbol](Symbol.spliceOwner.owner.owner)(using mv.symbolInstance)
    Expr(Symbol.spliceOwner.owner.owner.tree.toString)
//    val e = Expr("")
//    val term: Term = e.asTerm
//    val termRef = TermRef(TypeRepr.of[String], "firstParam")
//    val expr = Select.unique(Ident(TermRef(ThisType, "firstParam")),)
//    //Select.unique(Ident(termRef), "length").asExpr
//    Expr(expr.asTerm.toString)
//    Expr("hell")
//    //'{$expr.toString}

  }

}
