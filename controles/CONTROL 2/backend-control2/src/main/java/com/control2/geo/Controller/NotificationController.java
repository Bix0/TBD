package com.control2.geo.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Dto.TaskResponseDTO;
import com.control2.geo.Service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class NotificationController {

    private final TaskService taskService;

    @GetMapping("/notifications")
    public ResponseEntity<List<TaskResponseDTO>> taskNotifications(){
        List<TaskResponseDTO> list = new ArrayList<TaskResponseDTO>();
        list = taskService.verifyTaskByDate();
        if(list.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }
}
