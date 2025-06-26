package ru.common.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasks = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    public void addSubtask(Integer id) {
        this.subtasks.add(id);
    }

    public List<Integer> getSubtasks() {
        return this.subtasks;
    }

    public void setSubtasks(List<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    public void removeSubtaskById(int id) {
        subtasks.remove(id);
    }
}