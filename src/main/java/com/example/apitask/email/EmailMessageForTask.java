package com.example.apitask.email;

import java.io.Serializable;

public class EmailMessageForTask implements Serializable {
    private String to;
    private String userName;
    private String taskName;
    private String taskDueDate;

    public EmailMessageForTask() {}

    public EmailMessageForTask(String to, String userName, String taskName, String taskDueDate) {
        this.to = to;
        this.userName = userName;
        this.taskName = taskName;
        this.taskDueDate = taskDueDate;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }
}
