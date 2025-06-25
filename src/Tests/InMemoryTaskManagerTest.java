package Tests;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static service.Managers.getDefault;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = getDefault();
    }

    @Test
    void addAndGetTask() {
        Task task = new Task("Task1", "Description", Status.NEW);
        boolean added = manager.addTask(task);
        assertTrue(added);
        Task retrieved = manager.getTask(task.getId());
        assertEquals(task.getId(), retrieved.getId());
        assertEquals(task.getTitle(), retrieved.getTitle());
        assertEquals(task.getStatus(), retrieved.getStatus());
    }

    @Test
    void addAndGetEpic() {
        Epic epic = new Epic("Epic1", "Epic Description");
        boolean added = manager.addTask(epic);
        assertTrue(added);
        Epic retrieved = manager.getEpic(epic.getId());
        assertEquals(epic.getId(), retrieved.getId());
    }

    @Test
    void addAndGetSubtask() {
        Epic epic = new Epic("EpicWithSubtasks", "Desc");
        manager.addTask(epic);

        Subtask subtask = new Subtask("Subtask1", "SubDesc", Status.NEW, epic.getId());
        boolean added = manager.addTask(subtask);
        assertTrue(added);

        Subtask retrieved = manager.getSubtask(subtask.getId());
        assertEquals(subtask.getTitle(), retrieved.getTitle());

        ArrayList<Integer> subtasksIds = manager.getSubtasksOfEpic(epic.getId());
        assertTrue(subtasksIds.contains(subtask.getId()));
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("TaskToDelete", "Desc", Status.NEW);
        manager.addTask(task);
        boolean deleted = manager.deleteById(task.getId());
        assertTrue(deleted);
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void deleteEpicAndSubtasks() {
        Epic epic = new Epic("EpicToDelete", "Desc");
        manager.addTask(epic);

        Subtask s1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId());
        Subtask s2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId());
        manager.addTask(s1);
        manager.addTask(s2);

        boolean deleted = manager.deleteById(epic.getId());
        assertTrue(deleted);
        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(s1.getId()));
        assertNull(manager.getSubtask(s2.getId()));
    }

    @Test
    void updateTask() {
        Task task = new Task("OldTitle", "OldDesc", Status.NEW);
        manager.addTask(task);

        Task newTask = new Task("NewTitle", "NewDesc", Status.DONE);
        newTask.setId(task.getId()); // устанавливаем id для обновления
        boolean updated = manager.updateTask(newTask);
        assertTrue(updated);

        Task updatedTask = manager.getTask(task.getId());
        assertEquals("NewTitle", updatedTask.getTitle());
        assertEquals("NewDesc", updatedTask.getDescription());
        assertEquals(Status.DONE, updatedTask.getStatus());
    }

    @Test
    void updateEpicStatus() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addTask(epic);

        Subtask s1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId());
        Subtask s2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId());
        manager.addTask(s1);
        manager.addTask(s2);

        // Все новые - статус эпика NEW
        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus());

        // Обновим статус одной подзадачи на DONE
        s1.setStatus(Status.DONE);
        manager.updateTask(s1);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());

        // Обновим вторую подзадачу также на DONE
        s2.setStatus(Status.DONE);
        manager.updateTask(s2);
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void deleteAllTasksSubtasksEpics() {
        Task t = new Task("T", "Desc", Status.NEW);
        Epic e = new Epic("E", "Desc");
        manager.addTask(t);
        manager.addTask(e);

        Subtask s = new Subtask("S", "Desc", Status.NEW, e.getId());
        manager.addTask(s);

        manager.deleteAllTasks();
        assertTrue(manager.printTasks().isEmpty());
        assertFalse(manager.printEpics().isEmpty());

        manager.deleteAllSubtasks();
        assertTrue(manager.printSubtasks().isEmpty());

        manager.deleteAllEpics();
        assertTrue(manager.printEpics().isEmpty());
    }
}

