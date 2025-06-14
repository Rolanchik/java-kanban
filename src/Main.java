import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Manager;

public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();

        // Создаём и добавляем обычную задачу
        Task task1 = new Task("model.Task 1", "Description 1", Status.NEW);
        manager.addTask(task1);

        // Создаём и добавляем эпик
        Epic epic1 = new Epic("model.Epic 1", "model.Epic Description");
        manager.addEpic(epic1);

        // Создаём и добавляем подзадачи, связанные с эпиком
        Subtask subtask1 = new Subtask("model.Subtask 1", "model.Subtask Desc 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "model.Subtask Desc 2", Status.DONE, epic1.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        // Проверяем вывод задач
        System.out.println("Все задачи:");
        manager.printTasks();

        System.out.println("\nВсе эпики:");
        manager.printEpics();

        System.out.println("\nВсе подзадачи:");
        manager.printSubtasks();

        // Обновляем статус подзадачи и проверяем обновление статуса эпика
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        System.out.println("\nПосле обновления статуса подзадачи:");
        manager.printEpics();

        // Поиск по ID
        System.out.println("\nПоиск по ID 1:");
        manager.searchId(1);

        // Удаляем эпик вместе с подзадачами
        manager.deleteEpicById(epic1.getId());

        System.out.println("\nПосле удаления эпика и его подзадач:");
        manager.printEpics();
        manager.printSubtasks();
    }
}