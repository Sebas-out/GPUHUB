package com.GPUHUB.GPUHUB.configuracion;

import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service("trabajadorUserDetailsService")
public class TrabajadorUserDetailsService implements UserDetailsService {

    private final TrabajadorServicio trabajadorServicio;

    public TrabajadorUserDetailsService(TrabajadorServicio trabajadorServicio) {
        this.trabajadorServicio = trabajadorServicio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar por DNI o correo
        Trabajador trabajador = trabajadorServicio.buscarPorDni(username)
                .or(() -> trabajadorServicio.buscarPorCorreo(username))
                .orElseThrow(() -> new UsernameNotFoundException("Trabajador no encontrado con DNI/correo: " + username));

        // Verificar si el trabajador está activo
        if (trabajador.getEstado() == null || trabajador.getEstado() != 1) {
            throw new UsernameNotFoundException("El trabajador no tiene acceso activo");
        }

        // Verificar si la fecha de acceso es válida
        if (trabajador.getFechaAcceso() == null || trabajador.getFechaAcceso().isBefore(java.time.LocalDate.now())) {
            throw new UsernameNotFoundException("El acceso del trabajador ha expirado");
        }

        // Actualizar último acceso
        trabajador.actualizarUltimoAcceso();
        trabajadorServicio.actualizar(trabajador);

        return new User(
            trabajador.getDni(), // Usamos DNI como username
            trabajador.getPassword(),
            true, // enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            getAuthorities()
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRABAJADOR"));
    }
}
