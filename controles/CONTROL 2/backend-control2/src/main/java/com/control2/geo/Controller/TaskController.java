package com.control2.geo.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Dto.TaskRequest;
import com.control2.geo.Entity.Task;
import com.control2.geo.Service.TaskService;
import com.control2.geo.security.UserPrincipal;

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

    @PostMapping("/tasks/createtask/{userId}")
    public ResponseEntity<String> createTask(@PathVariable Long userId, @RequestBody TaskRequest task, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para crear una tarea.");
        }
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping ("/tasks/modifytask/{idTask}")
    public ResponseEntity<String> modifyTask(@PathVariable Long idTask, @RequestBody TaskRequest task, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(idTask)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar una tarea.");
        }
        return ResponseEntity.ok(taskService.modifyTask(idTask, task));
    }

    @DeleteMapping("/tasks/deletetask/{idTask}/{userId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long idTask, @PathVariable Long userId, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar una tarea.");
        }
        return ResponseEntity.ok(taskService.deleteTask(idTask));
    }

    @PutMapping("/tasks/completeTask/{idTask}")
    public ResponseEntity<String> completeTask(@PathVariable Long idTask, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(idTask)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para completar esta tarea.");
        }
        return ResponseEntity.ok(taskService.changeTaskStatus(idTask));
    }
}
