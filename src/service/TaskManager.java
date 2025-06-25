package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;

public interface TaskManager {

    boolean addTask(Task task);

    boolean containsIdEpic(int epicId); // Проверка существования эпика по ID

    ArrayList<Task> printTasks();
    ArrayList<Epic> printEpics();
    ArrayList<Subtask> printSubtasks();

    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);

    boolean deleteById(int id);

    void deleteAllTasks();
    void deleteAllSubtasks();
    void deleteAllEpics();

    boolean updateTask(Task newTask);

    ArrayList<Integer> getSubtasksOfEpic(int epicId);
}
