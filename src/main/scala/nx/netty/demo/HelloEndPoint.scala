package org.apache.spark

import org.apache.spark.rpc.{RpcCallContext, RpcEndpoint, RpcEnv}

class HelloEndPoint(override val rpcEnv: RpcEnv) extends RpcEndpoint {

  override def onStart(): Unit = {
    println(rpcEnv.address)
    println("Start HelloEndPoint")
  }

  override def receive: PartialFunction[Any, Unit] = {
    case SayHi(msg) => println(s"Receive Message : ${msg}")
  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    case SayHi(msg) => {
      println(s"Receive Message : ${msg}")
      context.reply(s" I'm Server , ${msg}")
    }
    case SayBye(msg) => {
      println(s"Receive Message : ${msg}")
      context.reply(s" I'm Server , ${msg}")
    }
  }

  override def onStop(): Unit = {
    println("Stop HelloEndPoint")
  }
}

case class SayHi(msg: Any)

case class SayBye(msg: Any)
