package com.app;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class ZKWatcher2 implements Watcher {
    private final ZooKeeper zk;
    private final String basePath;

    public ZKWatcher2(ZooKeeper zk, String basePath) {
        this.zk = zk;
        this.basePath = basePath;
        setupWatches(basePath);
    }

    private void setupWatches(String path) {
        try {
            // Ustaw watch na istnienie węzła
            zk.exists(path, this);

            // Ustaw watch na dzieci
            List<String> children = zk.getChildren(path, this);

            // Rekurencyjnie ustaw watch na wszystkich potomków
            for (String child : children) {
                String childPath = path + "/" + child;
                setupWatches(childPath);
            }
        } catch (KeeperException.NoNodeException e) {
            // Węzeł nie istnieje - obsłuż odpowiednio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (path == null) return;

        try {
            switch (event.getType()) {
                case NodeCreated:
                    System.out.println("Utworzono: " + path);
                    if (path.equals(basePath)) {
                        startExternalApp();
                    }
                    setupWatches(path); // Ustaw nowe watches dla nowego węzła
                    break;

                case NodeDeleted:
                    System.out.println("Usunięto: " + path);
                    if (path.equals(basePath)) {
                        stopExternalApp();
                    }
                    break;

                case NodeChildrenChanged:
                    System.out.println("Zmieniono dzieci w: " + path);
                    if (path.equals(basePath)) {
                        showChildrenCount();
                    }
                    // Aktualizuj watches dla zmienionej ścieżki
                    setupWatches(path);
                    break;

                case NodeDataChanged:
                    System.out.println("Zmieniono dane w: " + path);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startExternalApp() {
        System.out.println("Starting external app");
    }

    private void stopExternalApp() {
        System.out.println("Stopping external app");
    }

}