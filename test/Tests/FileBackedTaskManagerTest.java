package Tests;

import org.junit.jupiter.api.*;
import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;
import ru.common.service.FileBackedTaskManager;
import ru.common.service.InMemoryTaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static Path testFilePath;
    private FileBackedTaskManager fileBackedTaskManager;

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
        fileBackedTaskManager = new FileBackedTaskManager(testFilePath);
        fileBackedTaskManager.deleteAllTasks();
        fileBackedTaskManager.deleteAllEpics();
        fileBackedTaskManager.deleteAllSubtasks();
    }

    @Test
    void addTaskAndSaveTest() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        fileBackedTaskManager.addTask(task);

        assertFalse(fileBackedTaskManager.getTasks().isEmpty(), "Task должен быть добавлен");

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
        fileBackedTaskManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description Subtask", Status.IN_PROGRESS, epic.getId());
        fileBackedTaskManager.addSubtask(subtask);

        assertFalse(fileBackedTaskManager.getEpics().isEmpty());
        assertFalse(fileBackedTaskManager.getSubtasks().isEmpty());

        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    void saveAndLoadFromFileTest() throws IOException {
        Task task = new Task("Loaded Task", "Desc", Status.DONE);
        fileBackedTaskManager.addTask(task);

        Epic epic = new Epic("Loaded Epic", "Desc Epic");
        fileBackedTaskManager.addEpic(epic);

        Subtask subtask = new Subtask("Loaded Subtask", "Desc Subtask", Status.NEW, epic.getId());
        fileBackedTaskManager.addSubtask(subtask);

        fileBackedTaskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath.toFile());

        assertEquals(1, fileBackedTaskManager.getTasks().size(), "Должна быть загружена 1 задача");
        assertEquals(1, fileBackedTaskManager.getEpics().size(), "Должен быть загружен 1 эпик");
        assertEquals(1, fileBackedTaskManager.getSubtasks().size(), "Должен быть загружен 1 субтаск");

        Task loadedTask = fileBackedTaskManager.getTasks().get(0);
        assertEquals("Loaded Task", loadedTask.getTitle());
    }

    @Test
    void deleteTasksTest() {
        Task task = new Task("Delete Task", "Desc", Status.NEW);
        fileBackedTaskManager.addTask(task);

        assertFalse(fileBackedTaskManager.getTasks().isEmpty());

        fileBackedTaskManager.deleteAllTasks();
        assertTrue(fileBackedTaskManager.getTasks().isEmpty());
    }
}
