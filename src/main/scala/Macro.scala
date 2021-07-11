object Macro:
  inline def impl: String = ${doImpl}

  private def doImpl =
    symbolInstance
    ???

  lazy val symbolInstance: String = symbolInstance
