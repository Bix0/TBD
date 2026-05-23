package com.control2.geo.Controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        List<Task> tasks = taskService.getAllTasks(status, keyword);
        if (tasks.isEmpty()) {
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
    public ResponseEntity<String> createTask(@PathVariable Long userId, @RequestBody TaskRequest task,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para crear una tarea.");
        }
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping("/tasks/modifytask/{idTask}")
    public ResponseEntity<String> modifyTask(@PathVariable Long idTask, @RequestBody TaskRequest task,
                                             @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        //Se elimina la comparación errónea entre el ID de usuario y el ID de tarea.
        return ResponseEntity.ok(taskService.modifyTask(idTask, task));
    }

    @DeleteMapping("/tasks/deletetask/{idTask}/{userId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long idTask, @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar una tarea.");
        }
        return ResponseEntity.ok(taskService.deleteTask(idTask));
    }

    @PutMapping("/tasks/completeTask/{idTask}/{userId}")
    public ResponseEntity<String> completeTask(@PathVariable Long idTask, @PathVariable Long userId,
                                               @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        //Verifica que el usuario que inició sesión sea el mismo que intenta completar la tarea.
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para completar esta tarea.");
        }

        //Asegurar orden (idTask, userId) que espera la firma del método en TaskService.
        return ResponseEntity.ok(taskService.changeTaskStatus(idTask, userId));
    }

    @GetMapping("/tasks/reports/closest-pending/{userId}")
    public ResponseEntity<Task> getClosestPendingTask(@PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Task task = taskService.getClosestPendingTask(userId);
        if (task == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks/reports/most-completed-sector/{userId}/{radiusInKm}")
    public ResponseEntity<java.util.Map<String, Object>> getSectorWithMostCompletedTasksWithinRadius(
            @PathVariable Long userId,
            @PathVariable double radiusInKm,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        java.util.Map<String, Object> result = taskService.getSectorWithMostCompletedTasksWithinRadius(userId,
                radiusInKm);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/reports/average-distance/{userId}")
    public ResponseEntity<Double> getAverageDistanceOfCompletedTasks(@PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Double avgDistance = taskService.getAverageDistanceOfCompletedTasks(userId);
        return ResponseEntity.ok(avgDistance);
    }

    @GetMapping("/tasks/reports/pending-concentration")
    public ResponseEntity<List<Map<String, Object>>> getPendingTasksConcentrationPerSector() {
        List<Map<String, Object>> result = taskService.getPendingTasksConcentrationPerSector();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/reports/user-sector-counts")
    public ResponseEntity<List<Map<String, Object>>> getTasksCountPerUserAndSector() {
        List<Map<String, Object>> result = taskService.getTasksCountPerUserAndSector();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/reports/user-sector-counts/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getTasksCountByUserAndSector(@PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Map<String, Object>> result = taskService.getTasksCountByUserAndSector(userId);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
