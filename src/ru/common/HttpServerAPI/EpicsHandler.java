package ru.common.HttpServerAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.common.model.Epic;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method)) {
            handleGetEpic(exchange, path);
            return;
        }

        if ("POST".equals(method)) {
            handlePostEpic(exchange);
            return;
        }

        if ("DELETE".equals(method)) {
            handleDeleteEpic(exchange, path);
            return;
        }
    }

    private void handleGetEpic(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 2 && parts[1].equals("epics")) {
            String responce = gson.toJson(manager.getEpics());
            sendText(exchange, responce, 200);
            return;
        }

        if (parts.length == 3 && parts[1].equals("epics")) {
            try {
                int id = Integer.parseInt(parts[2]);
                Epic epic = manager.getEpic(id);
                if (epic != null) {
                    sendText(exchange, gson.toJson(epic), 200);
                } else {
                    sendNotFind(exchange, "Задача с " + id + " не найдена", 404);
                }
            } catch (NumberFormatException e) {
                sendNotFind(exchange, "Некорректный id задачи", 404);
            }
            return;
        }

        if (parts.length == 4 && parts[1].equals("epics") && parts[3].equals("subtasks")) {
            try {
                int id = Integer.parseInt(parts[2]);

                if (manager.getEpic(id) == null) {
                    sendNotFind(exchange, "Эпик с id " + id + " не найден", 404);
                    return;
                }

                List<Integer> subtaskIds = manager.getSubtasksOfEpic(id);
                sendText(exchange, gson.toJson(subtaskIds), 200);

            } catch (NumberFormatException e) {
                sendNotFind(exchange, "Некорректный id", 404);
            }
            return;
        }

    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        boolean result = false;
        if (manager.getEpic(epic.getId()) == null) {
            result = manager.addEpic(epic);
        }

        if (result) {
            sendText(exchange, gson.toJson(epic), 201);
        } else {
            sendHasInteractions(exchange, "Задача пересекается с существующими", 406);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length < 3) {
            sendNotFind(exchange, "ID не указан", 404);
            return;
        }

        if (parts.length == 3 && parts[1].equals("epics")) {
            try {
                int id = Integer.parseInt(parts[2]);
                boolean removed = manager.deleteEpicById(id);

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
