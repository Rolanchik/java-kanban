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

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerSubtaskTest {
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
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        // Создаем эпик для подзадач
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2",
                Status.IN_PROGRESS, epic.getId(), Duration.ofHours(2), LocalDateTime.now().plusHours(2));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), listType);
        assertEquals(2, subtasks.size());
    }

    @Test
    void shouldGetEmptySubtasksList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask returnedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(returnedSubtask);
        assertEquals(subtask.getId(), returnedSubtask.getId());
        assertEquals("Подзадача", returnedSubtask.getTitle());
    }

    @Test
    void shouldReturn404ForNonExistentSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("не найдена"));
    }

    @Test
    void shouldReturn404ForInvalidSubtaskId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Некорректный id"));
    }

    @Test
    void shouldCreateNewSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask newSubtask = new Subtask("Новая подзадача", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        String subtaskJson = gson.toJson(newSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(createdSubtask);
        assertEquals("Новая подзадача", createdSubtask.getTitle());

        assertEquals(1, manager.getSubtasks().size());
    }

    @Test
    void shouldUpdateExistingSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask existingSubtask = new Subtask("Исходная подзадача", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
        manager.addSubtask(existingSubtask);

        Subtask updatedSubtask = new Subtask("Обновленная подзадача", "Новое описание",
                Status.IN_PROGRESS, epic.getId(), Duration.ofHours(2), LocalDateTime.now().plusHours(1));
        updatedSubtask.setId(existingSubtask.getId());

        String subtaskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getSubtasks().size());

        Subtask retrievedSubtask = manager.getSubtask(existingSubtask.getId());
        assertEquals("Обновленная подзадача", retrievedSubtask.getTitle());
        assertEquals(Status.IN_PROGRESS, retrievedSubtask.getStatus());
    }

    @Test
    void shouldReturn406ForTimeConflict() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();

        Subtask firstSubtask = new Subtask("Первая подзадача", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(2), startTime);
        manager.addSubtask(firstSubtask);

        Subtask conflictingSubtask = new Subtask("Пересекающаяся подзадача", "Описание",
                Status.NEW, epic.getId(), Duration.ofHours(1), startTime.plusMinutes(30));
        String subtaskJson = gson.toJson(conflictingSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("пересекается"));
    }

    @Test
    void shouldReturn406ForInvalidEpicId() throws IOException, InterruptedException {
        // Пытаемся создать подзадачу для несуществующего эпика
        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, 999, Duration.ofHours(1), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }


    @Test
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
           Epic epic = new Epic("Тестовый эпик", "Описание эпика");
           manager.addEpic(epic);

           Subtask subtask = new Subtask("Подзадача для удаления", "Описание",
                   Status.NEW, epic.getId(), Duration.ofHours(1), LocalDateTime.now());
           manager.addSubtask(subtask);

           HttpClient client = HttpClient.newHttpClient();
           URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
           HttpRequest request = HttpRequest.newBuilder()
                   .uri(url)
                   .DELETE()
                   .build();

           HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

           assertEquals(200, response.statusCode());
           assertEquals("Удалено", response.body());
           assertNull(manager.getSubtask(subtask.getId()));
           assertEquals(0, manager.getSubtasks().size());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("не найдена"));
    }
}
