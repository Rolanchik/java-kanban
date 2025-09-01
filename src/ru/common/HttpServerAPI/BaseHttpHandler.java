package ru.common.HttpServerAPI;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final String GET = "GET";
    protected static final String POST = "POST";
    protected static final String DELETE = "DELETE";

    protected static final String TASKS_PATH = "tasks";
    protected static final String EPICS_PATH = "epics";
    protected static final String SUBTASKS_PATH = "subtasks";
    protected static final String HISTORY_PATH = "history";
    protected static final String PRIORITIZED_PATH = "prioritized";

    protected void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendNotFind(HttpExchange exchange, String message, int code) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendHasInteractions(HttpExchange exchange, String message, int code) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }
}
