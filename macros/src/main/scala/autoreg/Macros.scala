package autoreg

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ReadFrom(val confPath: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReadFrom.implAnnot
}

object ReadFrom {
  def  implAnnot(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._
    println("ReadFrom: Start...")
    annottees.head match {
      case q"""$mods class $cname[..$tparams] $ctorMods(..$params)
             extends AutoRegBundle with ..$parent {$self => ..$stats}""" => {
        val confFile = c.prefix.tree match {
          case q"new ReadFrom($path)" => c.eval[String](c.Expr(path))
          case _ =>
            c.abort(c.enclosingPosition,
                    "ReadFrom must provide the path of config file!")
        }
        println(s"Creating Auto Reg Bundle from Config ${confFile}")
        q"""
            $mods class $cname(..$params) extends AutoRegBundle {
              ..$stats
              val fooIn = Input(UInt(16.W))
              val fooOut = Output(UInt(16.W))
            }
          """
      }
      case _ => {
        c.abort(c.enclosingPosition,
                "Annotation ReadFrom only apply to AutoRegBundle inherited!")
      }
    }
  }
}
