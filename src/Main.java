public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();

        // Создаём и добавляем обычную задачу
        Task task1 = new Task("Task 1", "Description 1", Task.Status.NEW);
        manager.addTask(task1);

        // Создаём и добавляем эпик
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        manager.addEpic(epic1);

        // Создаём и добавляем подзадачи, связанные с эпиком
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Desc 1", Task.Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Desc 2", Task.Status.DONE, epic1.getId());
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
        subtask1.setStatus(Task.Status.DONE);
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