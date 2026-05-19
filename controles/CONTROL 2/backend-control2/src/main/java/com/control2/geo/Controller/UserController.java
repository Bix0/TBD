package com.control2.geo.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Dto.UserRequest;
import com.control2.geo.Entity.User;
import com.control2.geo.Service.UserService;
import com.control2.geo.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if(users.isEmpty()) {
            // Return 204 No Content if the list of users is empty
            return ResponseEntity.noContent().build();
        }
        // Return 200 OK with the list of users
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            // Return 404 Not Found if the user is not found
            return ResponseEntity.notFound().build();
        }
        // Return 200 OK with the user
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/profile/{id}")
    public ResponseEntity<String> modifyUser(@PathVariable Long id,@RequestBody UserRequest dto, 
                                            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar el perfil de otro usuario.");
        }
        String response = userService.modifyUser(id, dto);
        if(response.equals("Usuario modificado exitosamente")){
            return ResponseEntity.ok(response);
        }
        else{
            return ResponseEntity.badRequest().body(response);
        }   
    }

    @DeleteMapping("/users/profile/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar el perfil de otro usuario.");
        }
        String response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }
}
