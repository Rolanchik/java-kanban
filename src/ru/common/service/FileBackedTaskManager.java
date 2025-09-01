package ru.common.service;

import ru.common.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;
    private static final int idIndex = 0;
    private static final int taskTypeIndex = 1;
    private static final int titleIndex = 2;
    private static final int statusIndex = 3;
    private static final int descriptionIndex = 4;
    private static final int epicIdStrIndex = 5;
    private static final int durationIndex = 6;
    private static final int startTimeIndex = 7;

    public FileBackedTaskManager(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean addTask(Task task) {
        boolean added = super.addTask(task);
        save();
        return added;
    }

    @Override
    public boolean addEpic(Epic epic) {
        boolean added = super.addEpic(epic);
        save();
        return added;
    }

    @Override
    public boolean addSubtask(Subtask subtask) {
        boolean added = super.addSubtask(subtask);
        save();
        return added;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public boolean deleteTaskById(int id) {
        boolean added = super.deleteTaskById(id);
        save();
        return added;
    }

    @Override
    public boolean deleteEpicById(int id) {
        boolean added = super.deleteEpicById(id);
        save();
        return added;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        boolean added = super.deleteSubtaskById(id);
        save();
        return added;
    }

    @Override
    public boolean updateTask(Task newTask) {
        boolean added = super.updateTask(newTask);
        save();
        return added;
    }

    @Override
    public boolean updateEpic(Epic newEpic) {
        boolean added = super.updateEpic(newEpic);
        save();
        return added;
    }

    @Override
    public boolean updateSubtask(Subtask newSubtask) {
        boolean added = super.updateSubtask(newSubtask);
        save();
        return added;
    }

    public void save() {
        List<String> lines = new ArrayList<>();

        lines.add("id,type,name,status,description,epic,duration,startTime");

        for (Task task : super.getTasks()) {
            lines.add(toString(task));
        }

        for (Epic epic : super.getEpics()) {
            lines.add(toString(epic));
        }

        for (Subtask subtask : super.getSubtasks()) {
            lines.add(toString(subtask));
        }

        try {
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString(Task task) {
        String epicId = "";
        if (task.getTaskType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String durationMinutes = (task.getDuration() != null) ? String.valueOf(task.getDuration().toMinutes()) : "";

        String startTime = (task.getStartTime() != null) ? task.getStartTime().toString() : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                durationMinutes,
                startTime);
    }

    public Task fromString(String value) throws IllegalArgumentException {
        String[] parts = value.split(",", -1);
        int id = Integer.parseInt(parts[idIndex]);
        TaskType taskType = TaskType.valueOf(parts[taskTypeIndex]);
        String title = parts[titleIndex];
        Status status = Status.valueOf(parts[statusIndex]);
        String description = parts[descriptionIndex];
        String epicIdStr = parts[epicIdStrIndex];

        Duration duration = null;
        if (!parts[durationIndex].equals("") && !parts[durationIndex].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(parts[durationIndex]));
        }

        LocalDateTime startTime = null;
        if (!parts[startTimeIndex].equals("") && !parts[startTimeIndex].isEmpty()) {
            startTime = LocalDateTime.parse(parts[startTimeIndex]);
        }

        switch (taskType) {
            case TASK:
                Task task = new Task(title, description, status, duration, startTime);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                return epic;
            case SUBTASK:
                int epicId = epicIdStr.isEmpty() ? -1 : Integer.parseInt(epicIdStr);
                Subtask subtask = new Subtask(title, description, status, epicId, duration, startTime);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown this type: " + taskType);
        }
    }


    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) continue;
                Task task = manager.fromString(line);

                switch (task.getTaskType()) {
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        break;
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                }
            }

            for (Subtask sub : manager.subtasks.values()) {
                Epic epic = manager.epics.get(sub.getEpicId());
                if (epic != null) {
                    epic.addSubtask(sub.getId());
                }
            }

        } catch (IOException e) {
            throw new IOException("Ошибка загрузки из файла " + file, e);
        }

        return manager;
    }

}
