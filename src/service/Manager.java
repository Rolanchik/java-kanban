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

    public void printTasks() {
        for(Task task : this.tasks.values()) {
            System.out.println(task);
        }

    }

    public void printEpics() {
        for(Epic epic : this.epics.values()) {
            System.out.println(epic);
        }

    }

    public void printSubtasks() {
        for(Subtask subtask : this.subtasks.values()) {
            System.out.println(subtask);
        }

    }

    public void searchId(int id) {
        if (this.tasks.containsKey(id)) {
            System.out.println(this.tasks.get(id));
        } else if (this.epics.containsKey(id)) {
            System.out.println(this.epics.get(id));
        } else if (this.subtasks.containsKey(id)) {
            System.out.println(this.subtasks.get(id));
        } else {
            System.out.println("Такого айди нет.");
        }

    }

    public void deleteId(int id) {
        if (this.tasks.containsKey(id)) {
            this.tasks.remove(id);
        } else if (this.epics.containsKey(id)) {
            this.deleteEpicById(id);
        } else if (this.subtasks.containsKey(id)) {
            Subtask subtask = (Subtask)this.subtasks.remove(id);
            Epic epic = (Epic)this.epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskById(id);
                epic.updateStatus();
            }
        } else {
            System.out.println("Такого айди нет.");
        }

    }

    public void deleteEpicById(int epicId) {
        Epic epic = (Epic)this.epics.remove(epicId);
        if (epic != null) {
            for(Subtask subtask : epic.getSubtasks()) {
                this.subtasks.remove(subtask.getId());
            }
        }

    }

    public void updateTask(Task newTask) {
        int id = newTask.getId();
        if (this.tasks.containsKey(id)) {
            this.tasks.put(id, newTask);
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }

    }

    public void updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        if (this.epics.containsKey(id)) {
            newEpic.setSubtasks(this.getSubtasksOfEpic(id));
            newEpic.updateStatus();
            this.epics.put(id, newEpic);
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }

    }

    public void updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        if (this.subtasks.containsKey(id)) {
            this.subtasks.put(id, newSubtask);
            Epic epic = (Epic)this.epics.get(newSubtask.getEpicId());
            if (epic != null) {
                epic.updateStatus();
            }
        } else {
            System.out.println("Сабтаск с таким ID не найден.");
        }

    }

    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = (Epic)this.epics.get(epicId);
        if (epic != null) {
            return new ArrayList(epic.getSubtasks());
        } else {
            System.out.println("Эпик с таким ID не найден.");
            return new ArrayList();
        }
    }
}