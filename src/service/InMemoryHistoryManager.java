package service;

import model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    @Override
    public void addToHistory(Task task) {
        if (task == null) {
            return;
        }

        history.add(task);
        if (history.size() > 10) {
            history.removeFirst();
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
