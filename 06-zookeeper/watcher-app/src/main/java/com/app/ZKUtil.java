package com.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.*;

public class ZKUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ZooKeeper zk;

    public ZKUtil(ZooKeeper zk) {
        this.zk = zk;
    }

    static class Node {
        String name;
        List<Node> children = new ArrayList<>();

        Node(String name) {
            this.name = name;
        }
    }

    private Node buildTree(String path) throws KeeperException, InterruptedException {
        String name = path.equals("/") ? "/" : path.substring(path.lastIndexOf("/") + 1);
        Node node = new Node(name);
        List<String> children = zk.getChildren(path, false);
        Collections.sort(children, String.CASE_INSENSITIVE_ORDER);
        for (String child : children) {
            String childPath = path.equals("/") ? "/" + child : path + "/" + child;
            node.children.add(buildTree(childPath));
        }
        return node;
    }

    private void printTree(Node node, String indent) {
        LOGGER.info(indent + "+ " + node.name);
        for (Node child : node.children) {
            printTree(child, indent + "    ");
        }
    }

    public void printTree(String zNodeName) {
        try {
            if (zk.exists(zNodeName, false) == null) {
                LOGGER.info("zNode `{}` doesn't exist", zNodeName);
                return;
            }
            Node root = buildTree(zNodeName);
            printTree(root, "");
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Error while printing tree", e);
        }
    }

}
