package ru.common.HttpServerAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.common.model.Subtask;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (GET.equals(method)) {
            handleGetSubtask(exchange, path);
            return;
        }

        if (POST.equals(method)) {
            handlePostSubtask(exchange);
            return;
        }

        if (DELETE.equals(method)) {
            handleDeleteSubtask(exchange, path);
            return;
        }
    }

    private void handleGetSubtask(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 2 && parts[1].equals(SUBTASKS_PATH)) {
            String responce = gson.toJson(manager.getSubtasks());
            sendText(exchange, responce, 200);
            return;
        }

        if (parts.length == 3 && parts[1].equals(SUBTASKS_PATH)) {
            try {
                int id = Integer.parseInt(parts[2]);
                Subtask subtask = manager.getSubtask(id);
                if (subtask != null) {
                    sendText(exchange, gson.toJson(subtask), 200);
                } else {
                    sendNotFind(exchange, "Задача с " + id + " не найдена", 404);
                }
            } catch (NumberFormatException e) {
                sendNotFind(exchange, "Некорректный id задачи", 404);
            }
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        boolean result;
        if (!manager.getSubtasks().contains(subtask)) {
            result = manager.addSubtask(subtask);
        } else {
            result = manager.updateSubtask(subtask);
        }

        if (result) {
            sendText(exchange, gson.toJson(subtask), 201);
        } else {
            sendHasInteractions(exchange, "Задача пересекается с существующими", 406);
        }
    }


    private void handleDeleteSubtask(HttpExchange exchange, String path) throws IOException {
        try {
            String[] parts = path.split("/");

            System.out.println("=== DELETE SUBTASK DEBUG ===");
            System.out.println("Path: " + path);
            System.out.println("Parts: " + java.util.Arrays.toString(parts));
            System.out.println("Parts length: " + parts.length);

            if (parts.length != 3 || !parts[1].equals(SUBTASKS_PATH)) {
                System.out.println("Invalid path structure");
                sendNotFind(exchange, "Некорректный путь. Ожидается /subtasks/{id}", 400);
                return;
            }

            try {
                int id = Integer.parseInt(parts[2]);
                System.out.println("Parsed ID: " + id);

                // Проверим, существует ли подзадача
                Subtask existingSubtask = manager.getSubtask(id);
                System.out.println("Existing subtask: " + existingSubtask);

                if (existingSubtask == null) {
                    System.out.println("Subtask not found");
                    sendNotFind(exchange, "Задача с ID " + id + " не найдена", 404);
                    return;
                }

                System.out.println("About to call deleteSubtaskById");
                boolean deleted = manager.deleteSubtaskById(id);
                System.out.println("Delete result: " + deleted);

                if (deleted) {
                    System.out.println("Sending success response");
                    sendText(exchange, "Удалено", 200);
                } else {
                    System.out.println("Delete returned false");
                    sendNotFind(exchange, "Не удалось удалить задачу с ID " + id, 500);
                }

            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException: " + e.getMessage());
                sendNotFind(exchange, "Некорректный ID: " + parts[2], 400);
            }

        } catch (Exception e) {
            System.out.println("Exception in handleDeleteSubtask: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            sendNotFind(exchange, "Ошибка при удалении задачи: " + e.getMessage(), 500);
        }
    }
}
