package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.dto.RegistroUsuarioDTO;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.repositorio.TiendaRepositorio;
import com.GPUHUB.GPUHUB.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.util.List;

@Service("usuarioServicioImpl")
@Primary
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final TiendaRepositorio tiendaRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServicioImpl(UsuarioRepositorio usuarioRepositorio,
                             TiendaRepositorio tiendaRepositorio,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tiendaRepositorio = tiendaRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario guardar(RegistroUsuarioDTO registroDTO) {
        if (usuarioRepositorio.existsByCorreo(registroDTO.getCorreo())) {
            throw new RuntimeException("El correo electrónico ya está en uso");
        }
        
        if (usuarioRepositorio.existsByDni(registroDTO.getDni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }
        
        if (usuarioRepositorio.existsByTelefono(registroDTO.getTelefono())) {
            throw new RuntimeException("El número de teléfono ya está en uso");
        }
        
        if (tiendaRepositorio.existsByRuc(registroDTO.getRuc())) {
            throw new RuntimeException("Ya existe una tienda con este RUC");
        }
        
        if (tiendaRepositorio.existsByNombreTienda(registroDTO.getNombreTienda())) {
            throw new RuntimeException("Ya existe una tienda con este nombre");
        }
        
        Tienda tienda = new Tienda();
        tienda.setRuc(registroDTO.getRuc());
        tienda.setNombreTienda(registroDTO.getNombreTienda());
        tienda.setDireccion(registroDTO.getDireccion());
        // Procesar imagen de la tienda si se recibe
        if (registroDTO.getImagenTienda() != null && !registroDTO.getImagenTienda().isEmpty()) {
            try {
                tienda.setImagen(registroDTO.getImagenTienda().getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Error al procesar la imagen de la tienda: " + e.getMessage());
            }
        }
        tienda = tiendaRepositorio.save(tienda);
        
        Usuario usuario = new Usuario();
        usuario.setDni(registroDTO.getDni());
        usuario.setNombres(registroDTO.getNombres());
        usuario.setApellidos(registroDTO.getApellidos());
        usuario.setCorreo(registroDTO.getCorreo());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getContrasena()));
        usuario.setTelefono(registroDTO.getTelefono());
        usuario.setTienda(tienda);
        usuario.setPrimoFavorito(registroDTO.getPrimoFavorito());
        usuario.setAnimalFavorito(registroDTO.getAnimalFavorito());
        usuario.setLugarFavorito(registroDTO.getLugarFavorito());
        return usuarioRepositorio.save(usuario);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + correo));
    }

    @Override
    @Transactional
    public void actualizarUltimoAcceso(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }

    @Override
    public List<Usuario> listarUsuariosPorTienda(Tienda tienda) {
        return usuarioRepositorio.findByTienda(tienda);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return usuarioRepositorio.existsByCorreo(correo);
    }

    @Override
    public boolean existePorDni(String dni) {
        return usuarioRepositorio.existsByDni(dni);
    }

    @Override
    public boolean existePorTelefono(String telefono) {
        return usuarioRepositorio.existsByTelefono(telefono);
    }

    @Override
    public Tienda obtenerTiendaDeUsuario(Long idUsuario) {
        return usuarioRepositorio.findById(idUsuario)
                .map(Usuario::getTienda)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public List<Usuario> obtenerEmpleadosDeTienda(Long idTienda) {
        Tienda tienda = tiendaRepositorio.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));
        return usuarioRepositorio.findByTienda(tienda);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = buscarPorCorreo(username);
        
        return new User(
            usuario.getCorreo(),
            usuario.getPassword(),
            true,
            true,
            true,
            true,
            usuario.getAuthorities()
        );
    }
}
