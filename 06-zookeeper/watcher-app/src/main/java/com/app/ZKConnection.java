package com.app;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKConnection {
    private ZooKeeper zk;
    private final CountDownLatch connectionLatch = new CountDownLatch(1);

    public ZooKeeper connect(String connectString) throws InterruptedException, IOException {
        zk = new ZooKeeper(
                connectString,
                3000,
                watchedEvent -> {
                    if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
        );
        connectionLatch.await();
        return zk;
    }

    public void closeConnection() throws InterruptedException {
        zk.close(2000);
    }
}
