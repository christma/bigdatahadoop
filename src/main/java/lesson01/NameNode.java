package lesson01;

import java.io.IOException;

public class NameNode {

    public static void main(String[] args) throws IOException {
        NameNodeRpcServer rpcServer = new NameNodeRpcServer();
        rpcServer.createRpcServer();
    }

}
