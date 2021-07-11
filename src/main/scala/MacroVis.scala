import scala.quoted._
import java.util.IdentityHashMap

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  import q.reflect._

  given symbolInstance: Vis[q.reflect.Symbol] = new VisBuilder[Symbol]
    .attributeReference("mayBeOwner")(_.maybeOwner)
    .build()
}

case class Visualized(
  name: String,
  attributes: Map[String, () => Visualized],
)

type VisBuilderCtx = IdentityHashMap[AnyRef, Unit]
case class VisBuilder[A](
  private val nameFun: A => String = (_: A) => "noname",
  private val attrsFun: Vector[A => (String, () => Visualized)] = Vector.empty,
  private val attrRefsFun: Vector[A => (String, String)] = Vector.empty,
) {
  def name(f: A => String): VisBuilder[A] = copy(nameFun = f)
  def attribute[B](name: String)(f: A => B)(using v: => Vis[B]): VisBuilder[A] = copy(attrsFun = attrsFun :+ (a => name -> (() => v.vis(f(a)))))
  def attributeReference[B: Vis](name: String)(f: A => B)(using v: => Vis[B]): VisBuilder[A] = copy(attrRefsFun = attrRefsFun :+ (a => name -> v.name(f(a))))

  def build(): Vis[A] = new Vis[A] {
    override def vis(a: A): Visualized = vis(a, new IdentityHashMap[Any, Unit])
    override def name(a: A): String = nameFun(a)
    override def vis(a: A, ctx: IdentityHashMap[Any, Unit]): Visualized = {
      if ctx.containsKey(a) then return Visualized(nameFun(a) + " <cycle>", Map.empty)
      ctx.put(a, ())
      Visualized(
        name = nameFun(a),
        attributes = attrsFun.map(_(a)).toMap ++ attrRefsFun.map(_(a)).map {
          case (name, value) => name -> (() => Visualized(value, Map.empty))
        }.toMap,
      )
    }
  }
}

trait Vis[A] {
  def vis(a: A): Visualized
  def name(a: A): String = vis(a).name
  def vis(a: A, ctx: IdentityHashMap[Any, Unit]): Visualized = vis(a)
}
object Vis:
  given stringInstance: Vis[String] with
    override def vis(a: String): Visualized = Visualized(name = a, attributes = Map.empty)

  given optionInstance[A](using v: Vis[A]): Vis[Option[A]] with
    override def vis(a: Option[A]): Visualized = a match {
      case None => Visualized(name = "None", attributes = Map.empty)
      case Some(vv) => v.vis(vv)
    }
