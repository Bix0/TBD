package com.control2.geo.Service;

import org.springframework.stereotype.Service;

import com.control2.geo.Entity.User;
import com.control2.geo.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BcryptService bcryptService;

    public User createUser(UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setLocation(dto.getGeoPoint());
        user.setPassword(bcryptService.encriptarClave(dto.getPassword()));
        return userRepository.save(user);
    }

    public User modifyUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setLocation(dto.getGeoPoint());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(bcryptService.encriptarClave(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    public String deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        userRepository.delete(user);
        return "Usuario eliminado exitosamente";
    }
}
