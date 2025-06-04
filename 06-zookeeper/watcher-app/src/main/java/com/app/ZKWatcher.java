package com.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ZKWatcher implements Watcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SESSION_TIMEOUT_MS = 3000;
    private static final int CLOSE_TIMEOUT_MS = 5000;
    private final String zNodeName;
    private final String[] exec;
    private final ZooKeeper zk;
    private final ZKUtil util;
    private Process execProcess;

    public ZKWatcher(String hostPort, String zNodeName, String exec[]) throws IOException {
        this.zNodeName = zNodeName;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, SESSION_TIMEOUT_MS, this);
        util = new ZKUtil(zk);
    }

    public void start() {
        try {
            zk.addWatch(zNodeName, AddWatchMode.PERSISTENT_RECURSIVE);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        enterInputLoop();
    }

    private void enterInputLoop() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String input = br.readLine();
                if (input.equals("quit")) {
                    zk.close(CLOSE_TIMEOUT_MS);
                    killExecProcess();
                    break;
                } else if (input.equals("tree")) {
                    util.printTree(zNodeName);
                }
            } catch (IOException ignored) {
                // ignored
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String watchedEventPath = watchedEvent.getPath();
        if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            handleNodeCreatedEvent(watchedEventPath);
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            handleNodeDeletedEvent(watchedEventPath);
        }
    }

    private void handleNodeCreatedEvent(String watchedEventPath) {
        LOGGER.info("Created zNode `{}`", watchedEventPath);
        if (watchedEventPath.startsWith(zNodeName)) {
            if (watchedEventPath.length() == zNodeName.length()) {
                LOGGER.info("Running exec process `{}`", exec);
                try {
                    execProcess = Runtime.getRuntime().exec(exec);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    LOGGER.info("zNode `{}` has {} children", zNodeName, zk.getAllChildrenNumber(zNodeName));
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleNodeDeletedEvent(String watchedEventPath) {
        LOGGER.info("Deleted zNode `{}`", watchedEventPath);
        if (zNodeName.equals(watchedEventPath)) {
            killExecProcess();
        }
    }

    private void killExecProcess() {
        if (execProcess == null || !execProcess.isAlive()) {
            LOGGER.info("Exec process already dead");
        } else {
            LOGGER.info("Killing exec process");
            execProcess.destroy();
        }
    }
}
