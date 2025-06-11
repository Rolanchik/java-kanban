package model;

import java.util.Objects;

public class Task {
    private int id;
    private final String title;
    private final String description;
    private Status status;

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Task task = (Task)o;
            return this.id == task.id;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    public String toString() {
        int var10000 = this.id;
        return "model.Task{id=" + var10000 + ", title='" + this.title + "', description='" + this.description + "', status=" + String.valueOf(this.status) + "}";
    }

    public static enum Status {
        NEW,
        IN_PROGRESS,
        DONE;
    }
}
