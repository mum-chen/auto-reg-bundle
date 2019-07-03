package autoreg

import chisel3._

@ReadFrom("xxxx.csv")
class MyBundle extends AutoRegBundle

class AutoRegDemo extends Module {
  val io = IO(new Bundle {
    val reg = new MyBundle()
  })

  val tmp = RegNext(io.reg.fooIn)
  io.reg.fooOut := tmp
}

object AutoRegMain extends App {
  chisel3.Driver.execute(args, () => new AutoRegDemo)
}
