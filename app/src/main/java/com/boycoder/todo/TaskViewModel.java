package com.boycoder.todo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ViewModel {
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public void addTask(Task task) {
        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks != null) {
            List<Task> newTasks = new ArrayList<>(currentTasks);
            newTasks.add(task);
            tasksLiveData.setValue(newTasks);
        }
    }

    public void updateTask(Task updatedTask) {
        // Anti-pattern for Lesson 1: Mutable data structures and traditional loops
        // This will be refactored using Kotlin's map operator and immutability concepts
        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks != null) {
            List<Task> newTasks = new ArrayList<>(currentTasks);
            for (int i = 0; i < newTasks.size(); i++) {
                if (newTasks.get(i).getId().equals(updatedTask.getId())) {
                    newTasks.set(i, updatedTask);
                    break;
                }
            }
            tasksLiveData.setValue(newTasks);
        }
    }

    public void deleteTask(String taskId) {
        // Anti-pattern for Lesson 1: Using removeIf which mutates the collection directly
        // This will be refactored using Kotlin's filter operator
        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks != null) {
            List<Task> newTasks = new ArrayList<>(currentTasks);
            newTasks.removeIf(task -> task.getId().equals(taskId));
            tasksLiveData.setValue(newTasks);
        }
    }

    // --- The following manual loops are intentional for teaching Kotlin Collection operators ---

    public int getActiveTaskCount() {
        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks == null) return 0;
        
        int count = 0;
        for (Task task : currentTasks) {
            if (!task.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public List<Task> getActiveTasks() {
        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks == null) return new ArrayList<>();

        List<Task> activeTasks = new ArrayList<>();
        for (Task task : currentTasks) {
            if (!task.isCompleted()) {
                activeTasks.add(task);
            }
        }
        return activeTasks;
    }
}
