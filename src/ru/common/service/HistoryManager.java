package ru.common.service;

import ru.common.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface HistoryManager {

    final List<Task> history = new ArrayList<>();

    void addToHistory(Task task);

    void remove(int id);

    List<Task> getHistory();
}
