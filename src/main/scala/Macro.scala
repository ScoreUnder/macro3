import scala.quoted._

object Macro:
  inline def impl: String = ${doImpl}

  private def doImpl(using Quotes): Expr[String] =
    symbolInstance
    ???

  lazy val symbolInstance: String = symbolInstance
