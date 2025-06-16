package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Manager {
    private final HashMap<Integer, Task> tasks = new HashMap();
    private final HashMap<Integer, Epic> epics = new HashMap();
    private final HashMap<Integer, Subtask> subtasks = new HashMap();
    private int idCounter = 0;

    public void addTask(Task task) {
        task.setId(idCounter++);
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
    }

    public boolean addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return false;
        } else {
            subtask.setId(idCounter++);
            subtasks.put(subtask.getId(), subtask);
            epic.addSubtask(subtask.getId());
            updateStatus(epicId);
            return true;
        }
    }

    public boolean containsIdEpic(int epicId) {
        return epics.containsKey(epicId);
    }

    public ArrayList<Task> printTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> printEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> printSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Task serchIdTask(int id) {
        return tasks.get(id);
    }

    public Epic serchIdEpic(int id) {
        return epics.get(id);
    }

    public Subtask serchIdSubtask(int id) {
        return subtasks.get(id);
    }

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

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setStatus(Status.NEW);
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }


    public boolean updateTask(Task newTask) {
        int id = newTask.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, newTask);
            return true;
        }
        return false;
    }

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

    public ArrayList<Integer> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return epic.getSubtasks();
        } else {
            System.out.println("Эпик с таким ID не найден.");
            return new ArrayList();
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