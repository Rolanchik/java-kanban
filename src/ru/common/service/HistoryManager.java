package ru.common.service;

import ru.common.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface HistoryManager {

    final List<Task> history = new ArrayList<>();
    final int MAX_SIZE = 10;

    void addToHistory(Task task);

    List<Task> getHistory();
}
