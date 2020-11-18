package org.apache.spark


import nx.netty.demo.HelloRpcSettings
import org.apache.spark.rpc.RpcEnv
import org.apache.spark.sql.SparkSession


/**
 * @author 020972
 * @date 2020/11/18 13:43
 * @describe
 */

object RpcServerMain {
  def main(args: Array[String]): Unit = {

    val conf:SparkConf = new SparkConf()
    val sparkSession = SparkSession.builder().config(conf).master("local[*]").appName("NX RPC").getOrCreate()
    val sparkContext = sparkSession.sparkContext

    val sparkEnv = sparkContext.env

    val rpcEnv = RpcEnv
      .create(HelloRpcSettings.getName(), HelloRpcSettings.getHostName(),
        HelloRpcSettings.getHostName(), HelloRpcSettings.getPort(), conf,
        sparkEnv.securityManager, 1, false)


    val helloEndpoint = new HelloEndPoint(rpcEnv)

    rpcEnv.setupEndpoint(HelloRpcSettings.getName(), helloEndpoint)

    rpcEnv.awaitTermination()

  }
}
