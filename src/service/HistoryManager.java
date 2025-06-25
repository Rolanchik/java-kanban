package service;

import model.Task;

import java.util.ArrayList;

public interface HistoryManager {

    final ArrayList<Task> history = new ArrayList<>();

    void addToHistory(Task task);

    ArrayList<Task> getHistory();
}
