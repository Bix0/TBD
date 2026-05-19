package com.control2.geo.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Entity.Task;
import com.control2.geo.Service.TaskService;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        if(tasks.isEmpty()) {
            // Return 204 No Content if the list of tasks is empty
            return ResponseEntity.noContent().build();
        }
        // Return 200 OK with the list of tasks
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            // Return 404 Not Found if the task is not found
            return ResponseEntity.notFound().build();
        }
        // Return 200 OK with the task
        return ResponseEntity.ok(task);
    }
}
