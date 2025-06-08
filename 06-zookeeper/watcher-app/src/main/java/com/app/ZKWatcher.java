package com.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ZKWatcher implements Watcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String[] programNamePath;
    private Process programProcess;

    private final String zNodeName;
    private final ZooKeeper zk;
    private final ZKUtil printer;

    public ZKWatcher(String serverAddress, String zNodeName, String[] programNamePath) throws IOException {
        this.zNodeName = zNodeName;
        this.programNamePath = programNamePath;

        zk = new ZooKeeper(serverAddress, 3000, this);
        printer = new ZKUtil(zk);
    }

    public void startWatcher() {
        try {
            zk.addWatch(zNodeName, AddWatchMode.PERSISTENT_RECURSIVE);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException("Error adding watcher", e);
        }
        startProcessingUserRequests();
    }

    private void startProcessingUserRequests() {
        LOGGER.info("User input listener started. Type `tree` to print zNode tree or `quit` to exit.");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String input = br.readLine();

                if (input == null) {
                    LOGGER.warn("End of input stream detected. Exiting...");
                    break;
                }

                input = input.trim();
                switch (input) {
                    case "quit":
                        LOGGER.info("Quitting application...");
                        zk.close(5000);
                        stopProgramProcess();
                        return;

                    case "tree":
                        printer.printTree(zNodeName);
                        break;

                    default:
                        LOGGER.warn("Unknown command: `{}`", input);
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error processing user input", e);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String watchedEventPath = watchedEvent.getPath();
        if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            createNodeHandler(watchedEventPath);
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            deleteNodeHandler(watchedEventPath);
        }
    }

    private void createNodeHandler(String watchedEventPath) {
        LOGGER.info("Created zNode `{}`", watchedEventPath);

        if (!watchedEventPath.startsWith(zNodeName)) return;

        boolean isMainNode = watchedEventPath.length() == zNodeName.length();

        if (isMainNode) {
            LOGGER.info("Starting new process of name: `{}`", programNamePath[0]);
            try {
                programProcess = Runtime.getRuntime().exec(programNamePath);
            } catch (IOException e) {
                throw new RuntimeException("Error starting process", e);
            }
        } else {
            try {
                int childrenCount = zk.getAllChildrenNumber(zNodeName);
                LOGGER.info("Amount of zNode `{}` children is: {}", zNodeName, childrenCount);
            } catch (KeeperException | InterruptedException e) {
                throw new RuntimeException("Error logging zNode children amount", e);
            }
        }

    }

    private void deleteNodeHandler(String watchedEventPath) {
        LOGGER.info("Deleted zNode `{}`", watchedEventPath);

        if (zNodeName.equals(watchedEventPath)) {
            stopProgramProcess();
        }
    }

    private void stopProgramProcess() {
        if (programProcess == null || !programProcess.isAlive()) {
            LOGGER.info("Process is already stopped or was never started.");
            return;
        }

        LOGGER.info("Stopping running process...");
        try {
            programProcess.destroy();
            if (!programProcess.waitFor(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Process did not terminate in time. Forcing shutdown.");
                programProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while waiting for process to stop", e);
        }
    }
}
