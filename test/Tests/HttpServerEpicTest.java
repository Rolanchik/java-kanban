package Tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.HttpServerAPI.DurationAdapter;
import ru.common.HttpServerAPI.HttpTaskServer;
import ru.common.HttpServerAPI.LocalDateTimeAdapter;
import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerEpicTest {
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
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager);
        assertEquals(1, epicsFromManager.size());
        assertEquals("Эпик 1", epicsFromManager.get(0).getTitle());
    }

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.addEpic(epic1);
        manager.addEpic(epic2);

        System.out.println("Эпиков в менеджере: " + manager.getEpics().size());
        System.out.println("ID эпиков: " + manager.getEpics().stream()
                .map(Epic::getId).collect(Collectors.toList()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Epic>>() {}.getType();
        List<Epic> epics = gson.fromJson(response.body(), listType);
        assertNotNull(epics);
        assertEquals(2, epics.size());
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic returnedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(returnedEpic);
        assertEquals(epic.getId(), returnedEpic.getId());
        assertEquals("Эпик 1", returnedEpic.getTitle());
    }

    @Test
    void shouldReturnNotFoundForInvalidEpicId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidIdFormat() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldGetSubtasksOfEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now().plusHours(2));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundForEpicSubtasksWithInvalidId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getEpic(epic.getId()));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingWithInvalidIdFormat() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingWithoutId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldNotCreateEpicWhenExists() throws IOException, InterruptedException {
        Epic existingEpic = new Epic("Существующий эпик", "Описание");
        manager.addEpic(existingEpic);

        Epic duplicateEpic = new Epic("Дубликат эпика", "Описание дубликата");
        manager.addEpic(duplicateEpic);
        String epicJson = gson.toJson(duplicateEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик с подзадачами", "Описание эпика");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now().plusHours(2));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(2,epic.getSubtasks().size());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getEpic(epic.getId()));

        assertEquals(0, manager.getSubtasks().size());
    }
}