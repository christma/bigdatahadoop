package nx.akka.demo

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
 * @author 020972
 * @date 2020/11/18 10:10
 * @describe
 */

class MyNodeManager(val nmhostname: String, val resourcemanagerhostname: String,
                    val resourcemanagerport: Int, val memory: Int, val cpu: Int) extends Actor {


  var nodemanagerid = nmhostname
  var rmRef: ActorSelection = _

  override def preStart(): Unit = {
    rmRef = context.actorSelection(s"akka.tcp://${Constant.RMAS}@${resourcemanagerhostname}:${resourcemanagerport}/user/${Constant.RMA}")
    println(nodemanagerid + " 正在注册")
    rmRef ! RegisterNodeManager(nodemanagerid, memory, cpu)
  }


  override def receive: Receive = {
    case RegisteredNodeManager(masterURL) => {
      println(masterURL)
      import scala.concurrent.duration._
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 4000 millis, self, SendMessage)
    }
    case SendMessage => {
      rmRef ! Heartbeat(nodemanagerid)
      println(Thread.currentThread().getId)
    }
  }
}

object MyNodeManager {
  def main(args: Array[String]): Unit = {
    val HOSTNAME = args(0)
    val RM_HOSTNAME = args(1)
    val RM_PORT = args(2).toInt
    val NODEMANAGER_MEMORY = args(3).toInt
    val NODEMANAGER_CORE = args(4).toInt
    var NODEMANAGER_PORT = args(5).toInt
    var NMHOSTNAME = args(6)
    val str =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = ${HOSTNAME}
         |akka.remote.netty.tcp.port = ${NODEMANAGER_PORT}
      """.stripMargin
    val conf = ConfigFactory.parseString(str)
    val actorSystem = ActorSystem(Constant.NMAS, conf)
    actorSystem.actorOf(Props(new MyNodeManager(NMHOSTNAME, RM_HOSTNAME, RM_PORT, NODEMANAGER_MEMORY, NODEMANAGER_CORE)))
  }
}
