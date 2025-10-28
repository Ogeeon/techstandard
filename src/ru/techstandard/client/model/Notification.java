package ru.techstandard.client.model;

public interface Notification {
    String getTitle();
    void setTitle(String title);
    
    String getMessage();
    void setMessage(String message);
}