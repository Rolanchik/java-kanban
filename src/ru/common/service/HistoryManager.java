package ru.common.service;

import ru.common.model.Task;

public interface HistoryManager {
    void addToHistory(Task task);

    void remove(int id);

    Node getHead();
}
