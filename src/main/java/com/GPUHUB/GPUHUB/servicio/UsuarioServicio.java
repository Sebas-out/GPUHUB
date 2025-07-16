package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.dto.RegistroUsuarioDTO;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UsuarioServicio extends UserDetailsService {
    
    Usuario guardar(RegistroUsuarioDTO registroDTO);
    
    Usuario buscarPorCorreo(String correo);
    
    void actualizarUltimoAcceso(String correo);
    
    List<Usuario> listarUsuarios();
    
    List<Usuario> listarUsuariosPorTienda(Tienda tienda);
    
    void eliminarUsuario(Long id);
    
    Usuario actualizarUsuario(Usuario usuario);
    
    boolean existePorCorreo(String correo);
    
    boolean existePorDni(String dni);
    
    boolean existePorTelefono(String telefono);
    
    Tienda obtenerTiendaDeUsuario(Long idUsuario);
    
    List<Usuario> obtenerEmpleadosDeTienda(Long idTienda);
}
