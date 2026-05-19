package com.control2.geo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.control2.geo.Dto.TaskRequest;
import com.control2.geo.Entity.Task;
import com.control2.geo.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public String createTask(TaskRequest dto) {
        // Lógica para crear una nueva tarea
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus("Pendiente");
        task.setDueDate(dto.getDueDate());
        task.setGeoPoint(dto.getGeoPoint());
        // Guardar la tarea en la base de datos (usando un repositorio)
        taskRepository.save(task);
        return "Tarea creada exitosamente";
    }

    public String modifyTask(Long id, TaskRequest dto) {
        // Lógica para modificar una tarea existente
        Task task = getTaskById(id);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setGeoPoint(dto.getGeoPoint());
        // Guardar los cambios en la base de datos
        taskRepository.save(task);
        return "Tarea modificada exitosamente";
    }

    public String changeTaskStatus(Long id) {
        // Lógica para cambiar el estado de una tarea
        Task task = getTaskById(id);
        task.setStatus("Completada");
        // Guardar los cambios en la base de datos
        taskRepository.save(task);
        return "Tarea marcada como completada exitosamente";
    }

    public String deleteTask(Long id) {
        // Lógica para eliminar una tarea
        Task task = getTaskById(id);
        taskRepository.delete(task);
        return "Tarea eliminada exitosamente";
    }
}
