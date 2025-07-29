package Tests;

import org.junit.jupiter.api.*;
import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;
import ru.common.service.FileBackedTaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static Path testFilePath;
    private FileBackedTaskManager manager;

    @BeforeAll
    static void setupAll() throws IOException {
        testFilePath = Files.createTempFile("tasks", ".csv");
    }

    @AfterAll
    static void cleanupAll() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @BeforeEach
    void setup() {
        manager = new FileBackedTaskManager(testFilePath);
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    @Test
    void addTaskAndSaveTest() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        manager.addTask(task);

        assertFalse(manager.getTasks().isEmpty(), "Task должен быть добавлен");

        // Проверка, что файл реально сохранился и содержит данные
        try {
            String content = Files.readString(testFilePath);
            assertTrue(content.contains("Test Task"), "Файл должен содержать добавленную задачу");
        } catch (IOException e) {
            fail("Ошибка чтения файла");
        }
    }

    @Test
    void addEpicAndSubtaskTest() {
        Epic epic = new Epic("Test Epic", "Description Epic");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description Subtask", Status.IN_PROGRESS, epic.getId());
        manager.addSubtask(subtask);

        assertFalse(manager.getEpics().isEmpty());
        assertFalse(manager.getSubtasks().isEmpty());

        // Убедимся, что субтаск привязан к эпику
        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    void saveAndLoadFromFileTest() throws IOException {
        Task task = new Task("Loaded Task", "Desc", Status.DONE);
        manager.addTask(task);

        Epic epic = new Epic("Loaded Epic", "Desc Epic");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Loaded Subtask", "Desc Subtask", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        // Сохраняем, потом загружаем из файла
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath.toFile());

        assertEquals(1, loadedManager.getTasks().size(), "Должна быть загружена 1 задача");
        assertEquals(1, loadedManager.getEpics().size(), "Должен быть загружен 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должен быть загружен 1 субтаск");

        Task loadedTask = loadedManager.getTasks().get(0);
        assertEquals("Loaded Task", loadedTask.getTitle());
    }

    @Test
    void deleteTasksTest() {
        Task task = new Task("Delete Task", "Desc", Status.NEW);
        manager.addTask(task);

        assertFalse(manager.getTasks().isEmpty());

        manager.deleteAllTasks();
        assertTrue(manager.getTasks().isEmpty());
    }
}
