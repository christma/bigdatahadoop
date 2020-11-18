package nx.netty.demo

/**
 * @author 020972
 * @date 2020/11/18 13:51
 * @describe
 */

object HelloRpcSettings {

  val rpcName = "hello-rpc-service"
  val port = 9527
  val hostname = "localhost"

  def getName(): String = {
    rpcName
  }

  def getPort(): Int = {
    port
  }

  def getHostName(): String = {
    hostname
  }
}
