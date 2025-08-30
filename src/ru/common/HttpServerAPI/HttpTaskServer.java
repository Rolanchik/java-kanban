package ru.common.HttpServerAPI;

import com.sun.net.httpserver.HttpServer;
import ru.common.service.Managers;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + PORT);
    }

    public void close() {
        if (server != null) {
            server.stop(1);
            System.out.println("HTTP-сервер остановлен!");
        }
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer(Managers.getDefault()).start();
    }
}
