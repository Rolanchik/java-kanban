package Tests;

import org.junit.jupiter.api.*;
import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;
import ru.common.service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private static Path testFilePath;

    @BeforeAll
    static void beforeAll() throws IOException {
        testFilePath = Files.createTempFile("tasks", ".csv");
    }

    @AfterAll
    static void afterAll() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(testFilePath);
    }

    @Test
    void addTaskAndSaveTest() {
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofHours(1),
                LocalDateTime.of(2024, 7, 2, 11, 0));
        taskManager.addTask(task);


        assertFalse(taskManager.getTasks().isEmpty(), "Task должен быть добавлен");

        try {
            String content = Files.readString(testFilePath);
            assertTrue(content.contains("Test Task"), "Файл должен содержать добавленную задачу");
        } catch (IOException e) {
            fail("Ошибка чтения файла");
        }
    }

    @Test
    void shouldHandleNullValuesWhenSavingAndLoading() throws IOException {
        Task task = new Task("Task", "Description", Status.NEW, null, null);
        Epic epic = new Epic("Epic", "Description");
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId(), null, null);
        FileBackedTaskManager manager = new FileBackedTaskManager(Path.of("test.csv"));
        manager.addEpic(epic);
        manager.addTask(task);
        manager.addSubtask(subtask);
        manager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(new File("test.csv"));
        Task loadedTask = loaded.getTask(task.getId());
        assertNull(loadedTask.getDuration());
        assertNull(loadedTask.getStartTime());
        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertNull(loadedSubtask.getDuration());
        assertNull(loadedSubtask.getStartTime());
    }

    @Test
    void shouldRestoreEpicSubtaskLinks() throws IOException {
        Epic epic = new Epic("Epic", "Description");
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        FileBackedTaskManager manager = new FileBackedTaskManager(Path.of("test.csv"));
        manager.addEpic(epic);
        manager.addSubtask(subtask);
        manager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(new File("test.csv"));
        Epic loadedEpic = loaded.getEpic(epic.getId());
        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertNotNull(loadedEpic);
        assertNotNull(loadedSubtask);
        assertEquals(1, loadedEpic.getSubtasks().size());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());
    }

    @Test
    void addEpicAndSubtaskTest() {
        Epic epic = new Epic("Test Epic", "Description Epic");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description Subtask", Status.IN_PROGRESS,
                epic.getId(), Duration.ofHours(1), LocalDateTime.of(2024, 7, 2, 11, 0));
        taskManager.addSubtask(subtask);

        assertFalse(taskManager.getEpics().isEmpty());
        assertFalse(taskManager.getSubtasks().isEmpty());

        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    void saveAndLoadFromFileTest() throws IOException {
        Task task = new Task("Loaded Task", "Desc", Status.DONE, Duration.ofHours(1),
                LocalDateTime.of(2024, 7, 2, 11, 0));
        taskManager.addTask(task);

        Epic epic = new Epic("Loaded Epic", "Desc Epic");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Loaded Subtask", "Desc Subtask", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 7, 2, 11, 0));
        taskManager.addSubtask(subtask);

        taskManager.save();

        assertEquals(1, taskManager.getTasks().size(), "Должна быть загружена 1 задача");
        assertEquals(1, taskManager.getEpics().size(), "Должен быть загружен 1 эпик");
        assertEquals(1, taskManager.getSubtasks().size(), "Должен быть загружен 1 субтаск");

        Task loadedTask = taskManager.getTasks().get(0);
        assertEquals("Loaded Task", loadedTask.getTitle());
    }

    @Test
    void deleteTasksTest() {
        Task task = new Task("Delete Task", "Desc", Status.NEW, Duration.ZERO, LocalDateTime.now());
        taskManager.addTask(task);

        assertFalse(taskManager.getTasks().isEmpty());

        taskManager.deleteAllTasks();
        assertTrue(taskManager.getTasks().isEmpty());
    }
}
