package ru.common.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;

import static ru.common.service.Managers.getDefaultHistory;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager = getDefaultHistory();

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int idCounter = 0;

    @Override
    public boolean addTask(Task task) {
        if (task instanceof Subtask) {
            int epicId = ((Subtask) task).getEpicId();
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return false;
            } else {
                task.setId(idCounter++);
                subtasks.put(task.getId(), (Subtask) task);
                epic.addSubtask(task.getId());
                updateStatus(epicId);
                return true;
            }
        } else if (task instanceof Epic) {
            task.setId(idCounter++);
            epics.put(task.getId(), (Epic) task);
            return true;
        } else {
            task.setId(idCounter++);
            tasks.put(task.getId(), task);
            return true;
        }
    }

    @Override
    public boolean containsIdEpic(int epicId) {
        return epics.containsKey(epicId);
    }

    @Override
    public List<Task> printTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> printEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> printSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addToHistory(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addToHistory(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addToHistory(subtask);
        }
        return subtask;
    }

    public boolean deleteEpic(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for (Integer s : epic.getSubtasks()) {
                subtasks.remove(s);
            }
            return true;
        }
        return false;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public boolean deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            deleteEpic(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskById(id);
                updateStatus(epic.getId());
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean updateTask(Task newTask) {
        int id = newTask.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, newTask);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        Epic existingEpic = epics.get(id);
        if (existingEpic != null) {
            existingEpic.setTitle(newEpic.getTitle());
            existingEpic.setDescription(newEpic.getDescription());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        if (subtasks.containsKey(id)) {
            subtasks.put(id, newSubtask);
            Epic epic = epics.get(newSubtask.getEpicId());
            if (epic != null) {
                updateStatus(newSubtask.getEpicId());
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Integer> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return epic.getSubtasks();
        } else {
            System.out.println("Эпик с таким ID не найден.");
            return new ArrayList<>();
        }
    }

    public void updateStatus(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            boolean allDone = true;
            boolean allNew = true;

            for (Integer s : epic.getSubtasks()) {
                if (subtasks.get(s).getStatus() != Status.DONE) {
                    allDone = false;
                }
                if (subtasks.get(s).getStatus() != Status.NEW) {
                    allNew = false;
                }
            }

            if (allDone) {
                epic.setStatus(Status.DONE);
            } else if (allNew) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }
}
