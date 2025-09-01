package Tests;

import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;
import org.junit.jupiter.api.Test;
import ru.common.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static ru.common.service.Managers.getDefault;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    protected TaskManager createTaskManager() {
        return getDefault();
    }

    @Test
    void addAndGetTask() {
        Task task = new Task("Task1", "Description", Status.NEW, Duration.ZERO, LocalDateTime.now());
        boolean added = taskManager.addTask(task);
        assertTrue(added);
        Task retrieved = taskManager.getTask(task.getId());
        assertEquals(task.getId(), retrieved.getId());
        assertEquals(task.getTitle(), retrieved.getTitle());
        assertEquals(task.getStatus(), retrieved.getStatus());
        assertEquals(task.getStatus(), retrieved.getStatus());
        assertEquals(task.getDuration(), retrieved.getDuration());
        assertEquals(task.getStartTime(), retrieved.getStartTime());
        assertEquals(1, taskManager.getPrioritizedTasks().size());
        assertEquals(task, taskManager.getPrioritizedTasks().get(0));
    }

    @Test
    void getPrioritizedTasks_sortedByStartTime() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 7, 2, 11, 0));
        Task task2 = new Task("Task 2", "Desc", Status.NEW, Duration.ofHours(2),
                LocalDateTime.of(2024, 7, 1, 9, 0));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        var prioritized = taskManager.getPrioritizedTasks();
        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
    }

    @Test
    void addTask_intersection_returnsFalse() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW, Duration.ofHours(2),
                LocalDateTime.of(2024, 7, 1, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Status.NEW, Duration.ofHours(2),
                LocalDateTime.of(2024, 7, 1, 11, 0));

        assertTrue(taskManager.addTask(task1));
        assertFalse(taskManager.addTask(task2));
    }

    @Test
    void addAndGetEpic() {
        Epic epic = new Epic("Epic1", "Epic Description");
        boolean added = taskManager.addEpic(epic);
        assertTrue(added);
        Epic retrieved = taskManager.getEpic(epic.getId());
        assertEquals(epic.getId(), retrieved.getId());
    }


    @Test
    void addAndGetSubtask() {
        Epic epic = new Epic("EpicWithSubtasks", "Desc");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "SubDesc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        boolean added = taskManager.addSubtask(subtask);
        assertTrue(added);
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask));

        Subtask retrieved = taskManager.getSubtask(subtask.getId());
        assertEquals(subtask.getTitle(), retrieved.getTitle());

        ArrayList<Integer> subtasksIds = (ArrayList<Integer>) taskManager.getSubtasksOfEpic(epic.getId());
        assertTrue(subtasksIds.contains(subtask.getId()));
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("TaskToDelete", "Desc", Status.NEW, Duration.ZERO, LocalDateTime.now());
        taskManager.addTask(task);
        boolean deleted = taskManager.deleteTaskById(task.getId());
        assertTrue(deleted);
        assertNull(taskManager.getTask(task.getId()));
    }

    @Test
    void deleteEpicAndSubtasks() {
        Epic epic = new Epic("EpicToDelete", "Desc");
        taskManager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        Subtask s2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        taskManager.addSubtask(s1);
        taskManager.addSubtask(s2);

        boolean deleted = taskManager.deleteEpicById(epic.getId());
        assertTrue(deleted);
        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(s1.getId()));
        assertNull(taskManager.getSubtask(s2.getId()));
    }

    @Test
    void updateTask() {
        Task task = new Task("OldTitle", "OldDesc", Status.NEW, Duration.ZERO, LocalDateTime.now());
        taskManager.addTask(task);

        Task newTask = new Task("NewTitle", "NewDesc", Status.DONE, Duration.ZERO, LocalDateTime.now());
        newTask.setId(task.getId());
        boolean updated = taskManager.updateTask(newTask);
        assertTrue(updated);

        Task updatedTask = taskManager.getTask(task.getId());
        assertEquals("NewTitle", updatedTask.getTitle());
        assertEquals("NewDesc", updatedTask.getDescription());
        assertEquals(Status.DONE, updatedTask.getStatus());
    }

    @Test
    void updateEpicStatus() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        Subtask s2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId(), Duration.ZERO,
                LocalDateTime.now());
        taskManager.addSubtask(s1);
        taskManager.addSubtask(s2);

        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus());

        s1.setStatus(Status.DONE);
        taskManager.updateSubtask(s1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());

        s2.setStatus(Status.DONE);
        taskManager.updateSubtask(s2);
        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void deleteAllTasksSubtasksEpics() {
        Task t = new Task("T", "Desc", Status.NEW, Duration.ZERO, LocalDateTime.now());
        Epic e = new Epic("E", "Desc");
        taskManager.addTask(t);
        taskManager.addEpic(e);

        Subtask s = new Subtask("S", "Desc", Status.NEW, e.getId(), Duration.ZERO, LocalDateTime.now());
        taskManager.addSubtask(s);

        taskManager.deleteAllTasks();
        assertTrue(taskManager.printTasks().isEmpty());
        assertFalse(taskManager.printEpics().isEmpty());

        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.printSubtasks().isEmpty());

        taskManager.deleteAllEpics();
        assertTrue(taskManager.printEpics().isEmpty());
    }
}

