package com.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            LOGGER.error("Usage: java Main <host>:<port> <zNodeName> <execPath> <execArgs>*");
            System.exit(1);
        }

        String serverAddress = args[0];
        String zNodeName = args[1];

        String[] programNamePath = Arrays.copyOfRange(args, 2, args.length);

        LOGGER.info("Connecting to ZooKeeper at {}", serverAddress);
        LOGGER.info("Watching zNode `{}` and ready to launch `{}`", zNodeName, programNamePath[0]);

        ZKWatcher watcher = new ZKWatcher(serverAddress, zNodeName, programNamePath);
        watcher.startWatcher();
    }
}

