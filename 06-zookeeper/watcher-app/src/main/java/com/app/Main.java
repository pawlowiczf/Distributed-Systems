package com.app;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZKManagerImpl zkManager = new ZKManagerImpl();

        zkManager.create("/zk-node", "data");
        String data = zkManager.getZNodeData("/zk-node", true);
        System.out.println(data);

        zkManager.closeConnection();
    }

}
