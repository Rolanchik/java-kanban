package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
        this.updateStatus();
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.add(subtask);
        this.updateStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return this.subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
        this.updateStatus();
    }

    public void removeSubtaskById(int id) {
        for(int i = 0; i < this.subtasks.size(); ++i) {
            if ((this.subtasks.get(i)).getId() == id) {
                this.subtasks.remove(i);
                break;
            }
        }

        this.updateStatus();
    }

    public void updateStatus() {
        if (this.subtasks.isEmpty()) {
            this.setStatus(Status.NEW);
        } else {
            boolean allDone = true;

            for(Subtask s : this.subtasks) {
                if (s.getStatus() != Status.DONE) {
                    allDone = false;
                    break;
                }
            }

            if (allDone) {
                this.setStatus(Status.DONE);
            } else {
                this.setStatus(Status.IN_PROGRESS);
            }

        }
    }
}