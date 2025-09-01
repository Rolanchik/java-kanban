package ru.common.HttpServerAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.common.model.Task;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (HttpMethod.GET.equals(method)) {
            handleGetTask(exchange, path);
            return;
        }

        if (HttpMethod.POST.equals(method)) {
            handlePostTask(exchange);
            return;
        }

        if (HttpMethod.DELETE.equals(method)) {
            handleDeleteTask(exchange, path);
            return;
        }
    }

    private void handleGetTask(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 2 && parts[1].equals(TASKS_PATH)) {
            String responce = gson.toJson(manager.getTasks());
            sendText(exchange, responce, 200);
            return;
        }

        if (parts.length == 3 && parts[1].equals(TASKS_PATH)) {
            try {
                int id = Integer.parseInt(parts[2]);
                Task task = manager.getTask(id);
                if (task != null) {
                    sendText(exchange, gson.toJson(task), 200);
                } else {
                    sendNotFind(exchange, "Задача с " + id + " не найдена", 404);
                }
            } catch (NumberFormatException e) {
                sendNotFind(exchange, "Некорректный id задачи", 404);
            }
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        boolean result;
        Task existing = (task.getId() != 0) ? manager.getTask(task.getId()) : null;

        if (existing == null) {
            result = manager.addTask(task);
            if (result) {
                sendText(exchange, gson.toJson(task), 201);
            } else {
                sendHasInteractions(exchange, "Задача пересекается с существующими", 406);
            }
        } else {
            result = manager.updateTask(task);
            if (result) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendHasInteractions(exchange, "Задача пересекается с существующими", 406);
            }
        }
    }

    private void handleDeleteTask(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length < 3) {
            sendNotFind(exchange, "ID не указан", 404);
            return;
        }

        if (parts.length == 3 && parts[1].equals(TASKS_PATH)) {
            try {
                int id = Integer.parseInt(parts[2]);
                boolean removed = manager.deleteTaskById(id);

                if (removed) {
                    sendText(exchange, "Удалено", 200);
                } else {
                    sendNotFind(exchange, "Задача не найдена", 404);
                }
            } catch (NumberFormatException e) {
                sendNotFind(exchange, "Некорректный ID", 404);
            }
        }
    }
}
