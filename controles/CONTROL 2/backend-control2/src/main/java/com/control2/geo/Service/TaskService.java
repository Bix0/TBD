package com.control2.geo.Service;

import org.springframework.stereotype.Service;

import com.control2.geo.Entity.Task;
import com.control2.geo.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(TaskRequest dto) {
        // Lógica para crear una nueva tarea
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus("Pendiente");
        task.setDueDate(dto.getDueDate());
        task.setGeoPoint(dto.getGeoPoint());
        // Guardar la tarea en la base de datos (usando un repositorio)
        taskRepository.save(task);
        return task;
    }

    public Task modifyTask(Long id, TaskRequest dto) {
        // Lógica para modificar una tarea existente
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());
        task.setGeoPoint(dto.getGeoPoint());
        // Guardar los cambios en la base de datos
        taskRepository.save(task);
        return task;
    }

    public String deleteTask(Long id) {
        // Lógica para eliminar una tarea
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        taskRepository.delete(task);
        return "Tarea eliminada exitosamente";
    }
}
