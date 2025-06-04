package com.app;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZKManagerImpl implements ZKManager {
    private final ZooKeeper zk;
    private final ZKConnection zkConnection;

    public ZKManagerImpl() throws IOException, InterruptedException {
        zkConnection = new ZKConnection();
        zk = zkConnection.connect("localhost:2181");
    }

    public void closeConnection() throws InterruptedException {
        zkConnection.closeConnection();
    }

    @Override
    public void create(String path, String data) throws KeeperException, InterruptedException {
        zk.create(
                path,
                data.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL
        );
    }

    @Override
    public String getZNodeData(String path, boolean watchFlag) throws InterruptedException, KeeperException {
        byte[] data = null;
        data = zk.getData(path, null, null);
        return new String(data);
    }

    @Override
    public void update(String path, String data) throws KeeperException, InterruptedException {
        int version = zk.exists(path, true).getVersion();
        zk.setData(path, data.getBytes(), version);
    }
}
