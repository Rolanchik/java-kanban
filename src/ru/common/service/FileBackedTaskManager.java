package ru.common.service;

import ru.common.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;

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
        boolean added = super.updateTask(newEpic);
        save();
        return added;
    }

    @Override
    public boolean updateSubtask(Subtask newSubtask) {
        boolean added = super.updateTask(newSubtask);
        save();
        return added;
    }

    public void save() {
        List<String> lines = new ArrayList<>();

        lines.add("id,type,name,status,description,epic");

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
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    public Task fromString(String value) throws IllegalArgumentException {
        String[] parts = value.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType taskType = TaskType.valueOf(parts[1]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts[5];

        switch (taskType) {
            case TASK:
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                return epic;
            case SUBTASK:
                int epicId = epicIdStr.isEmpty() ? -1 : Integer.parseInt(epicIdStr);
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown this type: " + taskType);
        }

    }

    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager lastFile = new FileBackedTaskManager(file.toPath());

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) continue;
                Task task = lastFile.fromString(line);
                if (task instanceof Epic) {
                    lastFile.addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    lastFile.addSubtask((Subtask) task);
                } else {
                    lastFile.addTask(task);
                }
            }
        } catch (IOException e) {
            throw new IOException("Ошибка загрузки из файла " + file, e);
        }

        return lastFile;
    }
}
