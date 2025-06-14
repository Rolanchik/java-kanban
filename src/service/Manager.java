package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.Epic;
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
            epic.addSubtask(subtask);
            epic.updateStatus();
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

    public Object searchId(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        return null;
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
                epic.updateStatus();
            }
            return true;
        }
        return false;
    }

    public boolean deleteEpicById(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for(Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
            return true;
        }
        return false;
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
        if (epics.containsKey(id)) {
            newEpic.setSubtasks(getSubtasksOfEpic(id));
            newEpic.updateStatus();
            epics.put(id, newEpic);
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
                epic.updateStatus();
            }
            return true;
        }
        return false;
    }

    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return epic.getSubtasks();
        } else {
            System.out.println("Эпик с таким ID не найден.");
            return new ArrayList();
        }
    }
}