package com.control2.geo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.control2.geo.Dto.UserRequest;
import com.control2.geo.Entity.User;
import com.control2.geo.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BcryptService bcryptService;
    private final GeoPointService geoPointService;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String createUser(UserRequest dto) {
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setGeoPoint(geoPointService.createGeoPoint(dto.getLocationUser()));
        user.setPassword(bcryptService.encriptarClave(dto.getPassword()));
        userRepository.save(user);
        return "Usuario creado exitosamente";
    }

    public String modifyUser(Long id, UserRequest  dto) {
        User user = getUserById(id);
        user.setUserName(dto.getUserName());
        user.setGeoPoint(geoPointService.createGeoPoint(dto.getLocationUser()));
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(bcryptService.encriptarClave(dto.getPassword()));
            userRepository.save(user);
            return "Usuario modificado exitosamente";
        }
        else{
            
            return "Usuario sin modificar, la contrasenia no es valida";
        }
    }

    public String deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        return "Usuario eliminado exitosamente";
    }
}
