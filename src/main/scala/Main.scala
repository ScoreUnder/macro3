import java.io.RandomAccessFile

object Main {
  // Select(Ident(firstParam),length)
  // EmptyTree
  def main(args: Array[String]): Unit = {
    val f = new RandomAccessFile("src/main/scala/Main.scala", "rw")
    f.seek(f.length())
    f.writeByte('/')
    f.close()
    new Boo("42").boo
  }

  class Boo(val firstParam: String) {
    //def foo: String = firstParam.length.toString + firstParam.lines()
    def boo: Unit = println(Macro.impl)
  }
}
/////////////////