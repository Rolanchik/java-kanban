package ru.common.service;

import ru.common.model.Epic;
import ru.common.model.Subtask;
import ru.common.model.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    boolean addTask(Task task);

    boolean addEpic(Epic epic);

    boolean addSubtask(Subtask subtask);

    boolean containsIdEpic(int epicId); // Проверка существования эпика по ID

    List<Task> printTasks();

    List<Epic> printEpics();

    List<Subtask> printSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    boolean deleteTaskById(int id);

    boolean deleteEpicById(int id);

    boolean deleteSubtaskById(int id);

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    boolean updateTask(Task newTask);

    boolean updateEpic(Epic newEpic);

    boolean updateSubtask(Subtask newSubtask);

    List<Integer> getSubtasksOfEpic(int epicId);

    List<Task> getHistoryList();
}
