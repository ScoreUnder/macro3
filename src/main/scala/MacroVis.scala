import scala.quoted._
import java.util.IdentityHashMap

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  given symbolInstance: Vis[q.reflect.Symbol] = summon[Vis[q.reflect.Symbol]]
}

trait Vis[A]
