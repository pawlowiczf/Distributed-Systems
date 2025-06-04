package com.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ZKUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ZooKeeper zk;

    public ZKUtil(ZooKeeper zk) {
        this.zk = zk;
    }

    public void printTree(String zNodeName) {
        List<String> tree = getTree(zNodeName);
        for (String treePart : tree) {
            StringBuilder sb =  new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(treePart, "/");
            String token = null;
            while (tokenizer.hasMoreTokens()) {
                if (token != null) {
                    sb.append("    ");
                }
                token = tokenizer.nextToken();
            }
            sb.append("+-- ");
            sb.append(token);
            LOGGER.info(sb.toString());
        }
    }

    private List<String> getTree(String zNodeName) {
        List<String> tree = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        stack.push(zNodeName);
        while (!stack.empty()) {
            String current = stack.pop();
            try {
                List<String> children = zk.getChildren(current, false);
                // Sorting in reverse order because of stack usage
                children.sort(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
                children.forEach(ch -> stack.push(String.format("%s/%s", current, ch)));
                tree.add(current);
            } catch (KeeperException e) {
                if (e.code().name().equals("NONODE")) {
                    if (e.getPath().equals(zNodeName)) {
                        LOGGER.info("zNode `{}` doesn't exist", zNodeName);
                    } else {
                        LOGGER.warn("Expected zNode `{}` but was missing", e.getPath());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tree;
    }

}
