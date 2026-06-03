package com.autosur.models;

public class Task {
    public enum Status {
        Pending("Pendiente"),
        InProgress("En progreso"),
        Complete("Completada");

        private final String value;
        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromString(String text) {
            if (text == null) throw new IllegalArgumentException("El estado no puede ser nulo");
            for (Status b : Status.values()) {
                if (b.value.equalsIgnoreCase(text.trim()) || b.name().equalsIgnoreCase(text.trim())) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Estado desconocido en AutoSur: " + text);
        }
    }

    private int id;
    private String title;
    private String description;
    private int assignedUserId;
    private String assignedUsername;
    private Status status;

    public Task(int id, String title, String description, int assignedUserId, String assignedUsername, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedUserId = assignedUserId;
        this.assignedUsername = assignedUsername;
        this.status = status;
    }

    public Task(String title, String description, int assignedUserId, Status status) {
        this.title = title;
        this.description = description;
        this.assignedUserId = assignedUserId;
        this.status = status;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getAssignedUserId() { return assignedUserId; }
    public String getAssignedUsername() { return assignedUsername; }
    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }
}