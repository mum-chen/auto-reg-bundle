package test

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object helloMacro {
  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._
    val result = {
      annottees.map(_.tree).toList match {
        case q"object $name extends ..$parents { ..$body }" :: Nil =>
          q"""
            object $name extends ..$parents {
              def hello: ${typeOf[String]} = "hello"
              ..$body
            }
          """
      }
    }
    c.Expr[Any](result)
  }
}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class hello extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro helloMacro.impl
}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class TalkingAnimal(val voice: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro TalkingAnimal.implAnnot
}

object TalkingAnimal {
  def implAnnot(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._
    println("talking animal")
    annottees.head match {
      case q"$mods class $cname[..$tparams] $ctorMods(..$params) extends Animal with ..$parent {$self => ..$stats}" =>
        val voice = c.prefix.tree match {
          case q"new TalkingAnimal($sound)" => c.eval[String](c.Expr(sound))
          case _ =>
            c.abort(c.enclosingPosition, "TalkingAnimal must provide voice sample!")
        }

        val animlType = cname.toString()
        q"""
            $mods class $cname(..$params) extends Animal {
              ..$stats
              def sayHello: Unit =
                println("Hello, I'm a " + $animlType + " and my name is " + name + " " + $voice + "...")

            }
          """
      case _ =>
        c.abort(c.enclosingPosition,
                "Annotation TalkingAnimal only apply to Animal inherited!")
    }
  }
}
