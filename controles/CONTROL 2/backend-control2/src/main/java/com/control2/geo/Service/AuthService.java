package com.control2.geo.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.control2.geo.Dto.AuthResponse;
import com.control2.geo.Dto.LoginRequest;
import com.control2.geo.Dto.UserRequest;
import com.control2.geo.Entity.User;
import com.control2.geo.Repository.UserRepository;
import com.control2.geo.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(UserRequest dto) {
        if (userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        userService.createUser(dto);

        String token = jwtService.generateToken(dto.getUserName());
        return AuthResponse.builder()
                .token(token)
                .userName(dto.getUserName())
                .build();
    }

    public AuthResponse login(LoginRequest dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUserName(),
                        dto.getPassword()
                )
        );

        User user = userRepository.findByUserName(dto.getUserName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(user.getUserName());
        return AuthResponse.builder()
                .token(token)
                .userName(user.getUserName())
                .build();
    }
}
