package ru.common.model;

import ru.common.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.common.service.Managers.getDefault;

public class Epic extends Task {
    private final TaskManager taskManager = getDefault();
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
        subtasks.remove(id);
        recalcTimeFields();
    }

    public void recalcTimeFields() {
        if (subtasks.isEmpty()) {
            endTime = null;
            return;
        }

        Duration total = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        for (Integer s : subtasks) {
            Subtask sub = taskManager.getSubtask(s);
            if (sub != null) {
                total = total.plus(sub.getDuration());
            }

            LocalDateTime sStart = sub.getStartTime();
            LocalDateTime sEnd = sub.getEndTime();

            if (sStart != null && (minStart == null || sStart.isBefore(minStart))) {
                minStart = sStart;
            }

            if (sEnd != null && (maxEnd == null || sEnd.isAfter(maxEnd))) {
                maxEnd = sEnd;
            }
        }

        setDuration(total.equals(Duration.ZERO) ? null : total);
        setStartTime(minStart);
        endTime = maxEnd;
    }
}