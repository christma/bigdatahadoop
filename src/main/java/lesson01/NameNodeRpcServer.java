package lesson01;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;

import java.io.IOException;

public class NameNodeRpcServer implements ClientProtocol {
    public void mkdir(String path) {
        System.out.println("服务端：" + path);
    }

    public void createRpcServer() throws IOException {
        Server server = new RPC.Builder(new Configuration())
                .setBindAddress("")
                .setPort(9999)
                .setProtocol(ClientProtocol.class)
                .setInstance(new NameNodeRpcServer())
                .build();

        server.start();
    }

}
