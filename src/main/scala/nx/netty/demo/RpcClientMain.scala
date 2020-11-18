package org.apache.spark

import nx.netty.demo.HelloRpcSettings
import org.apache.spark.rpc.{RpcAddress, RpcEnv}
import org.apache.spark.sql.SparkSession
import scala.concurrent.duration.Duration

import scala.concurrent.Await


/**
 * @author 020972
 * @date 2020/11/18 15:10
 * @describe
 */

object RpcClientMain {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    val sparkSession = SparkSession.builder().config(conf).master("local[2]").appName("client rpc").getOrCreate()
    val sparkContext = sparkSession.sparkContext
    val sparkEnv = sparkContext.env


    val rpcEnv = RpcEnv.create(HelloRpcSettings.getName(), HelloRpcSettings.getHostName(), HelloRpcSettings.getPort(), conf, sparkEnv.securityManager, false)

    val endPointRef = rpcEnv.setupEndpointRef(RpcAddress(HelloRpcSettings.getHostName(), HelloRpcSettings.getPort()), HelloRpcSettings.getName())

    endPointRef.send(SayHi("client send"))

    val future = endPointRef.ask[String](SayHi("client ask"))
    import scala.concurrent.ExecutionContext.Implicits.global

    future.onComplete {
      case scala.util.Success(value) => println(s"Got the Ask result = $value")
      case scala.util.Failure(e) => println(s"Got the Ask error: $e")
    }

    Await.result(future, Duration.apply("20s"))

    val res = endPointRef.askSync[String](SayBye("client askSync"))

    println(res)

    sparkSession.stop()


  }
}
