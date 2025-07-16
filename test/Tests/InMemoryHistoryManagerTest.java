package Tests;

import ru.common.model.Status;
import ru.common.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.service.HistoryManager;
import ru.common.service.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.common.service.Managers.getDefault;
import static ru.common.service.Managers.getDefaultHistory;

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
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t1, history.getFirst());
    }

    @Test
    void addToHistoryReAddsTaskMovesToEnd() {
        Task task1 = new Task("1", "Task 1", Status.NEW);
        Task task2 = new Task("2", "Task 2", Status.NEW);
        Task task3 = new Task("3", "Task 3", Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        historyManager.addToHistory(task3);

        // Повторное добавление task2 — он должен сдвинуться в конец
        historyManager.addToHistory(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
        assertEquals(task2.getId(), history.get(2).getId());
    }

    @Test
    void removeTaskRemovesFromHistory() {
        Task task1 = new Task("1", "Task 1", Status.NEW);
        Task task2 = new Task("2", "Task 2", Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);

        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2.getId(), history.getFirst().getId());
    }
}
