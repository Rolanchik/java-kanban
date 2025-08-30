package ru.common.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description, Status.NEW, null, null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
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
        subtasks.remove(Integer.valueOf(id));
    }

}