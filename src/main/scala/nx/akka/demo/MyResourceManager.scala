package nx.akka.demo

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable

/**
 * @author 020972
 * @date 2020/11/18 9:27
 * @describe
 */

class MyResourceManager(var hostname: String, var port: Int) extends Actor {

  private var id2nodemanagerinfo = new mutable.HashMap[String, NodeManagerInfo]()

  private var nodemanagerInfos = new mutable.HashSet[NodeManagerInfo]()

  override def preStart(): Unit = {
    import scala.concurrent.duration._
    import context.dispatcher

    context.system.scheduler.schedule(0 millis, 5000 millis, self, CheckTimeOut)
  }


  override def receive: Receive = {
    case RegisterNodeManager(nodemanagerid, memory, cpu) => {
      val nodeManagerInfo = new NodeManagerInfo(nodemanagerid, memory, cpu)
      println(s"节点 ${nodemanagerid} 上线")
      id2nodemanagerinfo.put(nodemanagerid, nodeManagerInfo)
      nodemanagerInfos += nodeManagerInfo

      sender() ! RegisteredNodeManager(hostname + " : " + port)

    }
    case Heartbeat(nodemanagerid) => {
      val currentTime = System.currentTimeMillis()
      val nodeManagerInfo = id2nodemanagerinfo(nodemanagerid)
      nodeManagerInfo.lastHeartBeatTime = currentTime;

      id2nodemanagerinfo(nodemanagerid) = nodeManagerInfo
      nodemanagerInfos += nodeManagerInfo
    }
    case CheckTimeOut => {
      val currentTime = System.currentTimeMillis()
      nodemanagerInfos.filter(nm => {
        val heartbeatTimeout = 15000
        val bool = currentTime - nm.lastHeartBeatTime > heartbeatTimeout
        if (bool) {
          println(s"节点 ${nm.nodemanagerid} 下线")
        }
        bool
      }).foreach(deadnm => {
        nodemanagerInfos -= deadnm
        id2nodemanagerinfo.remove(deadnm.nodemanagerid)
      })
      println("当前注册成功的节点数" + nodemanagerInfos.size + "\t分别是：" +
        nodemanagerInfos.map(x => x.toString).mkString(","))
    }
  }
}


object MyResourceManager {
  def main(args: Array[String]): Unit = {
    val str =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = localhost
         |akka.remote.netty.tcp.port = 6789
      """.stripMargin

    val conf = ConfigFactory.parseString(str)

    val actorSystem = ActorSystem(Constant.RMAS, conf)


    actorSystem.actorOf(Props(new MyResourceManager("localhost", 6789)), Constant.RMA)
  }
}
