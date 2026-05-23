package com.control2.geo.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.control2.geo.Dto.TaskRequest;
import com.control2.geo.Dto.TaskResponseDTO;
import com.control2.geo.Entity.Task;
import com.control2.geo.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final GeoPointService geoPointService;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public List<Task> getAllTasks(String status, String keyword) {
        String formattedKeyword = (keyword != null && !keyword.trim().isEmpty())
                ? "%" + keyword.trim().toLowerCase() + "%"
                : null;
        return taskRepository.searchTasks(status, formattedKeyword);
    }

    public String createTask(TaskRequest dto) {
        // Lógica para crear una nueva tarea
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus("Pendiente");
        task.setDueDate(dto.getDueDate());
        if (dto.getIdGeoPoint() != null) {
            task.setGeoPoint(geoPointService.getGeoPointById(dto.getIdGeoPoint()));
        }
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
        if (dto.getIdGeoPoint() != null) {
            task.setGeoPoint(geoPointService.getGeoPointById(dto.getIdGeoPoint()));
        }
        // Guardar los cambios en la base de datos
        taskRepository.save(task);
        return "Tarea modificada exitosamente";
    }

    public String changeTaskStatus(Long idTask, Long idUser) {
        // Lógica para cambiar el estado de una tarea
        Task task = getTaskById(idTask);
        userService.addTaskToUser(idUser, task);
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

    /*
     * public List<TaskResponseDTO> verifyTaskByDate(){
     * List<Task> taskList = getAllTasks("Pendiente", null);
     * List<TaskResponseDTO> list = new ArrayList<TaskResponseDTO>();
     * LocalDate today = LocalDate.now();
     * for (Task task : taskList) {
     * if (list.size() >= 5) {
     * break;
     * }
     * long daysBetween = ChronoUnit.DAYS.between(today, task.getDueDate());
     * if(daysBetween <= 1 && daysBetween >= 0 ){
     * TaskResponseDTO task2 = new TaskResponseDTO();
     * task2.setId(task.getIdTask());
     * task2.setStatus(task.getStatus());
     * task2.setTitle(task.getTitle());
     * list.add(task2);
     * }
     * }
     * return list;
     * }
     */
    public List<TaskResponseDTO> verifyTaskByDate() {
        LocalDate today = LocalDate.now();
        List<Task> taskList = getAllTasks("Pendiente", null);

        return taskList.stream()
                .filter(task -> {
                    long days = ChronoUnit.DAYS.between(today, task.getDueDate());
                    return days >= 0 && days <= 1;
                })
                .limit(5)
                .map(task -> {
                    TaskResponseDTO dto = new TaskResponseDTO();
                    dto.setId(task.getIdTask());
                    dto.setStatus(task.getStatus());
                    dto.setTitle(task.getTitle());
                    return dto;
                })
                .toList();
    }

    public Task getClosestPendingTask(Long userId) {
        return taskRepository.findClosestPendingTask(userId);
    }

    public java.util.Map<String, Object> getSectorWithMostCompletedTasksWithinRadius(Long userId, double radiusInKm) {
        double radiusInMeters = radiusInKm * 1000.0;
        return taskRepository.findSectorWithMostCompletedTasksWithinRadius(userId, radiusInMeters);
    }

    public Double getAverageDistanceOfCompletedTasks(Long userId) {
        return taskRepository.getAverageDistanceOfCompletedTasks(userId);
    }

    public List<java.util.Map<String, Object>> getPendingTasksConcentrationPerSector() {
        return taskRepository.getPendingTasksConcentrationPerSector();
    }

    public List<Map<String, Object>> getTasksCountPerUserAndSector() {
        return userService.getTasksCountPerUserAndSector();
    }

    public List<Map<String, Object>> getTasksCountByUserAndSector(Long userId) {
        return taskRepository.countTasksByUserAndSector(userId);
    }
}
