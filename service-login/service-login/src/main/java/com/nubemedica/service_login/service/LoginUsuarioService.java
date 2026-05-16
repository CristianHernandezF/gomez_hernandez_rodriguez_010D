package com.nubemedica.service_login.service;

import com.nubemedica.service_login.dto.LoginUsuarioResponse;
import com.nubemedica.service_login.exceptions.AuthException;
import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.repository.LoginUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoginUsuarioService {

    @Autowired
    private LoginUsuarioRepository userRepository;

    public Optional<LoginUsuario> buscarPorCorreo(String correo) {
        return userRepository.findByCorreo(correo);
    }

    public Optional<LoginUsuario> buscarPorRunDoctor(String runDoctor) {
        return userRepository.findByRunDoctor(runDoctor);
    }

    public Optional<LoginUsuario> buscarPorTelefono(String numTelefono) {
        return userRepository.findByNumTelefono(numTelefono);
    }

    public void guardarUsuario(LoginUsuario usuario) {
        userRepository.save(usuario);
    }

    public List<LoginUsuarioResponse> listarUsuarios() {
        return userRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public LoginUsuarioResponse obtenerUsuario(Long id) {
        LoginUsuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        return convertirRespuesta(usuario);
    }

    public LoginUsuarioResponse actualizarContrasenaUsuario(Long id, String contrasenaEncriptada) {
        LoginUsuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        usuario.setContrasena(contrasenaEncriptada);
        userRepository.save(usuario);

        return convertirRespuesta(usuario);
    }

    public void eliminarUsuario(Long id) {
        LoginUsuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        userRepository.delete(usuario);
    }

    private LoginUsuarioResponse convertirRespuesta(LoginUsuario usuario) {
        return new LoginUsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                usuario.getNumTelefono(),
                usuario.getRunDoctor()
        );
    }
}