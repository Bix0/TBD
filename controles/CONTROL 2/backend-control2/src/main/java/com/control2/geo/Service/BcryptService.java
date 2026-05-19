package com.control2.geo.Service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class BcryptService {

    public String encriptarClave(String contrasena){
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }

    public boolean verificarClave(String contrasenaOriginal, String hashContrasena){
        return BCrypt.checkpw(contrasenaOriginal, hashContrasena);
    }
}
