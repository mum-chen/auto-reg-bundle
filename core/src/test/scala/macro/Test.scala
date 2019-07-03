package test

trait Animal {
  val name: String
}

@TalkingAnimal("wangwang")
case class Dog(val name: String) extends Animal

@TalkingAnimal("miaomiao")
case class Cat(val name: String) extends Animal

@hello
object MacroTest extends App {
  println(this.hello)
  Dog("Goldy").sayHello
  Cat("Kitty").sayHello
}
