package ru.common.service;

import java.time.LocalDateTime;
import java.util.*;

import ru.common.model.Epic;
import ru.common.model.Status;
import ru.common.model.Subtask;
import ru.common.model.Task;

import static ru.common.service.Managers.getDefaultHistory;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager = getDefaultHistory();

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int idCounter = 0;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparingInt(Task::getId)
    );

    private boolean isIntersect(Task a, Task b) {
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;

        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private boolean hasIntersectionWithAny(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) return false;
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isIntersect(t, task));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }


    @Override
    public boolean addTask(Task task) {
        if (task == null) return false;
        task.setId(idCounter++);

        if (task.getStartTime() != null && task.getDuration() != null && hasIntersectionWithAny(task)) return false;

        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) prioritizedTasks.add(task);

        return true;
    }


    @Override
    public boolean addEpic(Epic epic) {
        if (epic != null) {
            epic.setId(idCounter++);
            epics.put(epic.getId(), epic);
            return true;
        }

        return false;
    }


    @Override
    public boolean addSubtask(Subtask subtask) {
        if (subtask == null) return false;

        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) return false;

        subtask.setId(idCounter++);

        if (subtask.getStartTime() != null && subtask.getDuration() != null && hasIntersectionWithAny(subtask)) {
            return false;
        }

        subtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);

        epic.addSubtask(subtask.getId());
        epic.recalcTimeFields();
        updateStatus(epic.getId());
        return true;
    }

    @Override
    public boolean containsIdEpic(int epicId) {
        return epics.containsKey(epicId);
    }

    @Override
    public List<Task> printTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> printEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> printSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addToHistory(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addToHistory(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addToHistory(subtask);
        }
        return subtask;
    }

    private boolean deleteEpic(Epic remove) {
        if (remove != null) {
            for (Integer s : remove.getSubtasks()) {
                prioritizedTasks.remove(remove);
                historyManager.remove(s);
                subtasks.remove(s);
            }
            return true;
        }
        return false;
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }

        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Task subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subId : epic.getSubtasks()) {
                historyManager.remove(subId);
            }
            prioritizedTasks.remove(epic);
            historyManager.remove(epic.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task remove = tasks.remove(id);

        if (remove != null) {
            prioritizedTasks.remove(remove);
            historyManager.remove(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteEpicById(int id) {
        Epic remove = epics.remove(id);

        if (remove != null) {
            deleteEpic(remove);
            prioritizedTasks.remove(remove);
            historyManager.remove(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask remove = subtasks.remove(id);

        if (remove != null) {
            Epic epic = epics.get(remove.getEpicId());
            if (epic != null) {
                epic.removeSubtaskById(id);
                updateStatus(epic.getId());
            }
            prioritizedTasks.remove(remove);
            historyManager.remove(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean updateTask(Task newTask) {
        int id = newTask.getId();
        Task existingTask = tasks.get(id);

        if (existingTask == null) return false;

        if (newTask.getStartTime() != null && newTask.getDuration() != null && hasIntersectionWithAny(newTask)) {
            return false;
        }

        prioritizedTasks.remove(existingTask);

        existingTask.setTitle(newTask.getTitle());
        existingTask.setDescription(newTask.getDescription());
        existingTask.setStatus(newTask.getStatus());
        existingTask.setStartTime(newTask.getStartTime());
        existingTask.setDuration(newTask.getDuration());

        if (existingTask.getStartTime() != null) prioritizedTasks.add(existingTask);

        return true;
    }

    @Override
    public boolean updateEpic(Epic newEpic) {
        int id = newEpic.getId();
        Epic existingEpic = epics.get(id);
        if (existingEpic != null) {
            existingEpic.setTitle(newEpic.getTitle());
            existingEpic.setDescription(newEpic.getDescription());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        Subtask existingSubtask = subtasks.get(id);

        if (existingSubtask == null) return false;

        if (newSubtask.getStartTime() != null && newSubtask.getDuration() != null
                && hasIntersectionWithAny(newSubtask)) {
            return false;
        }

        prioritizedTasks.remove(existingSubtask);

        existingSubtask.setTitle(newSubtask.getTitle());
        existingSubtask.setDescription(newSubtask.getDescription());
        existingSubtask.setStatus(newSubtask.getStatus());
        existingSubtask.setStartTime(newSubtask.getStartTime());
        existingSubtask.setDuration(newSubtask.getDuration());

        Epic epic = epics.get(newSubtask.getEpicId());

        if (epic == null) return false;

        updateStatus(newSubtask.getEpicId());
        epic.recalcTimeFields();

        if (existingSubtask.getStartTime() != null) prioritizedTasks.add(existingSubtask);

        return true;
    }

    @Override
    public List<Integer> getSubtasksOfEpic(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtasks)
                .orElseGet(ArrayList::new);
    }

    @Override
    public List<Task> getHistoryList() {
        return historyManager.getHistory();
    }

    public void updateStatus(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            boolean allDone = true;
            boolean allNew = true;

            for (Integer s : epic.getSubtasks()) {
                if (subtasks.get(s).getStatus() != Status.DONE) {
                    allDone = false;
                }
                if (subtasks.get(s).getStatus() != Status.NEW) {
                    allNew = false;
                }
            }

            if (allDone) {
                epic.setStatus(Status.DONE);
            } else if (allNew) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }
}
