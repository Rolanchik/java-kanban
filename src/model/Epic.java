package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasks = new ArrayList();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    public void addSubtask(Integer id) {
        this.subtasks.add(id);
    }

    public ArrayList<Integer> getSubtasks() {
        return this.subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    public void removeSubtaskById(int id) {
        subtasks.remove(id);
    }


}