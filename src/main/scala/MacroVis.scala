import scala.quoted._
import java.util.IdentityHashMap

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  given symbolInstance: Vis[q.reflect.Symbol] = new VisBuilder[q.reflect.Symbol]
    .attributeReference[q.reflect.Symbol]
    .build()
}

case class Visualized(
  attributes: Map[String, () => Visualized],
)

type VisBuilderCtx = IdentityHashMap[AnyRef, Unit]
case class VisBuilder[A](
  private val attrRefsFun: Vector[(String, String)] = Vector.empty,
):
  def attributeReference[B: Vis]: VisBuilder[A] = this

  def build(): Vis[A] = new Vis[A]:
    override def vis(a: A): Visualized =
      Visualized(
        attrRefsFun.map {
          case (name, value) => name -> (() => ???)
        }.toMap,
      )

trait Vis[A]:
  def vis(a: A): Visualized
