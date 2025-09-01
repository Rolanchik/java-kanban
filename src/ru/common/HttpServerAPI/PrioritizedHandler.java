package ru.common.HttpServerAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (GET.equals(method)) {
            handleGetPrioritized(exchange, path);
            return;
        }
    }

    private void handleGetPrioritized(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 2 && parts[1].equals(PRIORITIZED_PATH)) {
            String responce = gson.toJson(manager.getPrioritizedTasks());
            sendText(exchange, responce, 200);
            return;
        }
    }
}
