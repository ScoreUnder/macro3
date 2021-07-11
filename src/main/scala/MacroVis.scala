import scala.quoted._
import java.util.IdentityHashMap

class MacroVis[Q <: Quotes & Singleton](using val q: Q) {
  import q.reflect._

  def vis[A: Vis](a: A): Visualized = summon[Vis[A]].vis(a)

  given symbolInstance: Vis[q.reflect.Symbol] = new VisBuilder[Symbol]
    .attribute("fullName")(_.fullName)
    .attributeReference("mayBeOwner")(_.maybeOwner)
    .attributeReference("privateWithin")(_.privateWithin)
    .attributeReference("protectedWithin")(_.protectedWithin)
//    .attribute("pos")(_.pos)
    .attribute("docstring")(_.docstring.map(_.take(10)))
    .children("annotations")(_.annotations)
    .children("declaredFields")(_.declaredFields)
    .children("memberFields")(_.memberFields)
    .children("declaredMethods")(_.declaredMethods)
    .children("memberMethods")(_.memberMethods)
    .children("declaredTypes")(_.declaredTypes)
    .children("memberTypes")(_.memberTypes)
    .children("declarations")(_.declarations)
    // paramSymss
    .children("allOverriddenSymbols")(_.allOverriddenSymbols.toSeq)
    .attribute("primaryConstructor")(_.primaryConstructor)
    .children("caseFields")(_.caseFields)
    .build()
    
  given typeReprInstance: Vis[q.reflect.TypeRepr] = new VisBuilder[TypeRepr]
    .name(_ => "name")
    .build()

  given termInstance: Vis[q.reflect.Term] = new VisBuilder[Term]
    .name(_ => "name")
    .build()

  println(symbolInstance)
}

case class Visualized(
  name: String,
  attributes: Map[String, () => Visualized],
  children: Map[String, () => Seq[Visualized]]
)

type VisBuilderCtx = IdentityHashMap[AnyRef, Unit]
case class VisBuilder[A](
  private val nameFun: A => String = (_: A) => "noname",
  private val attrsFun: Vector[A => (String, () => Visualized)] = Vector.empty,
  private val attrRefsFun: Vector[A => (String, String)] = Vector.empty,
  private val childrenFun: Vector[A => (String, () => Seq[Visualized])] = Vector.empty,
) {
  def name(f: A => String): VisBuilder[A] = copy(nameFun = f)
  def attribute[B](name: String)(f: A => B)(using v: => Vis[B]): VisBuilder[A] = copy(attrsFun = attrsFun :+ (a => name -> (() => v.vis(f(a)))))
  def attributeReference[B: Vis](name: String)(f: A => B)(using v: => Vis[B]): VisBuilder[A] = copy(attrRefsFun = attrRefsFun :+ (a => name -> v.name(f(a))))
  def children[B](name: String)(f: A => Seq[B])(using v: => Vis[B]): VisBuilder[A] = copy(childrenFun = childrenFun :+ (a => name -> (() => f(a).map(v.vis))))

  def build(): Vis[A] = new Vis[A] {
    override def vis(a: A): Visualized = vis(a, new IdentityHashMap[Any, Unit])
    override def name(a: A): String = nameFun(a)
    override def vis(a: A, ctx: IdentityHashMap[Any, Unit]): Visualized = {
      if ctx.containsKey(a) then return Visualized(nameFun(a) + " <cycle>", Map.empty, Map.empty)
      ctx.put(a, ())
      Visualized(
        name = nameFun(a),
        attributes = attrsFun.map(_(a)).toMap ++ attrRefsFun.map(_(a)).map {
          case (name, value) => name -> (() => Visualized(value, Map.empty, Map.empty))
        }.toMap,
        children = childrenFun.map(_(a)).toMap
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
    override def vis(a: String): Visualized = Visualized(name = a, attributes = Map.empty, children = Map.empty)

  given optionInstance[A](using v: Vis[A]): Vis[Option[A]] with
    override def vis(a: Option[A]): Visualized = a match {
      case None => Visualized(name = "None", attributes = Map.empty, children = Map.empty)
      case Some(vv) => v.vis(vv)
    }
