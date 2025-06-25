package Tests;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.TaskManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static service.Managers.getDefault;
import static service.Managers.getDefaultHistory;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private TaskManager manager;
    private Task t1;

    @BeforeEach
    void setUp() {
        historyManager = getDefaultHistory();
        manager = getDefault();
        t1 = new Task("Task", "Description", Status.NEW);
    }

    @Test
    void addToHistoryNullTaskDoesNothing() {
        historyManager.addToHistory(null);
        assertTrue(historyManager.getHistory().isEmpty(), "История должна оставаться пустой при добавлении" +
                " null");
    }

    @Test
    void addToHistoryAddsTask() {
        historyManager.addToHistory(t1);
        ArrayList<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t1, history.get(0));
        assertTrue(t1.getId() > 0, "ID должен быть установлен");
    }

    @Test
    void addToHistoryKeepsMaxTenTasks() {
        for (int i = 1; i <= 12; i++) {
            Task task = new Task("Task "+i, "Desc "+i, Status.NEW);
            manager.addTask(task);
            manager.getTask(task.getId());
        }
        ArrayList<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Размер истории не должен превышать 10");

        // Проверяем, что первые два добавленных таска удалились
        assertEquals(2, history.get(0).getId());
        assertEquals(11, history.get(9).getId());
    }
}
