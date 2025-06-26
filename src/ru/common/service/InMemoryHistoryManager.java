package ru.common.service;

import ru.common.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    @Override
    public void addToHistory(Task task) {
        if (task == null) {
            return;
        }

        history.add(task);
        if (history.size() > MAX_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
