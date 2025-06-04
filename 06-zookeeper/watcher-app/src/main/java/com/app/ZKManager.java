package com.app;

import org.apache.zookeeper.KeeperException;

public interface ZKManager {
    public void create(String path, String data) throws KeeperException, InterruptedException;
    public String getZNodeData(String path, boolean watchFlag) throws KeeperException, InterruptedException;
    public void update(String path, String data) throws KeeperException, InterruptedException;
}
