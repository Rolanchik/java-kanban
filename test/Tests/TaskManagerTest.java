package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;
import ru.common.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setup() {
        taskManager = createTaskManager();
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();
    }

    @Test
    void addTaskTest() {
        Task task = new Task("Task 1", "Desc", Status.NEW, Duration.ofHours(2),
                LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.addTask(task);
        assertFalse(taskManager.getTasks().isEmpty());
    }

    @Test
    void addEpicAndSubtasksTest() {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Status.NEW, epic.getId(), Duration.ofHours(2),
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Status.NEW, epic.getId(), Duration.ofHours(2),
                LocalDateTime.of(2025, 1, 1, 10, 0));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(epic.getId(), subtask1.getEpicId());
        assertEquals(2, taskManager.getSubtasks().size());
    }

    @Test
    void epicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("S1", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));
        taskManager.addSubtask(new Subtask("S2", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));

        assertEquals(Status.NEW, taskManager.getEpics().get(0).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("S1", "Desc", Status.DONE, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));
        taskManager.addSubtask(new Subtask("S2", "Desc", Status.DONE, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));

        assertEquals(Status.DONE, taskManager.getEpics().get(0).getStatus());
    }

    @Test
    void epicStatusNewAndDone() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("S1", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));
        taskManager.addSubtask(new Subtask("S2", "Desc", Status.DONE, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));

        assertEquals(Status.IN_PROGRESS, taskManager.getEpics().get(0).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("S1", "Desc", Status.IN_PROGRESS, epic.getId(), Duration.ZERO,
                LocalDateTime.now()));

        assertEquals(Status.IN_PROGRESS, taskManager.getEpics().get(0).getStatus());
    }

    @Test
    void subtaskHasEpic() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        taskManager.addSubtask(subtask);

        assertTrue(taskManager.getEpics().contains(epic));
        assertEquals(epic.getId(), subtask.getEpicId());
    }
}