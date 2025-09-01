package Tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import ru.common.HttpServerAPI.DurationAdapter;
import ru.common.HttpServerAPI.HttpTaskServer;
import ru.common.HttpServerAPI.LocalDateTimeAdapter;
import ru.common.model.Status;
import ru.common.model.Task;
import ru.common.service.InMemoryTaskManager;
import ru.common.service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTaskTest {
    private static TaskManager manager;
    private static HttpTaskServer server;
    private static Gson gson;

    @BeforeAll
    static void setUpClass() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        server.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Сервер запущен для всех тестов");
    }

    @AfterAll
    static void tearDownClass() {
        if (server != null) {
            server.close();
            System.out.println("Сервер остановлен");
        }
    }

    @BeforeEach
    void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        manager.getHistoryList().clear();
        manager.getPrioritizedTasks().clear();
    }

    @Test
    public void testAddTaskSuccess() throws IOException, InterruptedException {
        Task task = new Task("TestTask", "Description", Status.NEW,
                Duration.ofMinutes(10), LocalDateTime.now());

        String json = gson.toJson(task);
        System.out.println("Sending JSON: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());


        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    public void testGetAllTasks200() throws IOException, InterruptedException {
        Task task = new Task("Task2", "Desc", Status.NEW,
                Duration.ofMinutes(5), LocalDateTime.now());
        manager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Task>>(){}.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);
        assertEquals(1, tasks.size());
    }

    @Test
    public void testGetTaskById200() throws IOException, InterruptedException {
        Task task = new Task("SingleTask", "Check", Status.NEW,
                Duration.ofMinutes(20), LocalDateTime.now());
        manager.addTask(task);
        int id = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetTaskById404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTask200() throws IOException, InterruptedException {
        Task task = new Task("DeleteMe", "Desc", Status.NEW,
                Duration.ofMinutes(15), LocalDateTime.now());
        manager.addTask(task);
        int id = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void testDeleteTask404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/12345"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testAddTask406IfIntersection() throws IOException, InterruptedException {

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);

        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofMinutes(30), startTime);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofMinutes(30), startTime);

        String json = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }
}
