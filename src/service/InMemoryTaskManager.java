package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import static service.Managers.getDefaultHistory;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager = getDefaultHistory();

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
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
    public ArrayList<Task> printTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> printEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> printSubtasks() {
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

    @Override
    public boolean deleteById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return true;
        } else if (epics.containsKey(id)) {
            deleteEpicById(id);
            return true;
        } else if (subtasks.containsKey(id)) {
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

    public boolean deleteEpicById(int epicId) {
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
    public boolean updateTask(Task newTask) {
        if (newTask instanceof Subtask) {
            int id = newTask.getId();
            if (subtasks.containsKey(id)) {
                subtasks.put(id, (Subtask) newTask);
                Epic epic = epics.get(((Subtask) newTask).getEpicId());
                if (epic != null) {
                    updateStatus(((Subtask) newTask).getEpicId());
                }
                return true;
            }
            return false;
        } else if (newTask instanceof Epic) {
            int id = newTask.getId();
            Epic existingEpic = epics.get(id);
            if (existingEpic != null) {
                existingEpic.setTitle(newTask.getTitle());
                existingEpic.setDescription(newTask.getDescription());
                return true;
            }
            return false;
        } else {
            int id = newTask.getId();
            if (tasks.containsKey(id)) {
                tasks.put(id, newTask);
                return true;
            }
            return false;
        }
    }

    @Override
    public ArrayList<Integer> getSubtasksOfEpic(int epicId) {
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