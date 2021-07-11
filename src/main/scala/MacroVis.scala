import scala.quoted._
import java.util.IdentityHashMap

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  import q.reflect._

  given symbolInstance: Vis[q.reflect.Symbol] = new VisBuilder[Symbol]
    .attributeReference("mayBeOwner")(_.maybeOwner)
    .build()
}

case class Visualized(
  attributes: Map[String, () => Visualized],
)

type VisBuilderCtx = IdentityHashMap[AnyRef, Unit]
case class VisBuilder[A](
  private val attrRefsFun: Vector[A => (String, String)] = Vector.empty,
):
  def attributeReference[B: Vis](name: String)(f: A => B): VisBuilder[A] = copy(attrRefsFun = attrRefsFun :+ (a => name -> "name"))

  def build(): Vis[A] = new Vis[A]:
    override def vis(a: A): Visualized =
      Visualized(
        attrRefsFun.map(_(a)).map {
          case (name, value) => name -> (() => ???)
        }.toMap,
      )

trait Vis[A]:
  def vis(a: A): Visualized
