package ru.common.service;

import ru.common.model.Task;

import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> nodes = new HashMap<>();
    private Node head = null;
    private Node tail = null;

    @Override
    public void addToHistory(Task task) {
        if (task == null) {
            return;
        }

        if (nodes.containsKey(task.getId())) {
            remove(task.getId());
        }

        linkLast(task);
    }

    public void remove(int id) {
        if (nodes.containsKey(id)) {
            removeNode(nodes.get(id));
        }
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        nodes.remove(node.task.getId());
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        nodes.put(task.getId(), newNode);
    }

    @Override
    public Node getHead() {
        return head;
    }
}