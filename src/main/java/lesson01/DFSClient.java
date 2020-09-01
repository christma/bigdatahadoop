package lesson01;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.net.InetSocketAddress;

public class DFSClient {
    public static void main(String[] args) throws Exception {
        ClientProtocol clientProtocol = RPC.getProxy(
                ClientProtocol.class,
                1234L,
                new InetSocketAddress("localhost", 9999),
                new Configuration());


        clientProtocol.mkdir("/hello/world");
    }
}
