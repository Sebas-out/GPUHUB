document.addEventListener('DOMContentLoaded', function() {
    // Elementos del DOM
    const btnAgregarTrabajador = document.getElementById('btnAgregarTrabajador');
    const contenedorFormulario = document.getElementById('formularioTrabajador');
    const formularioTrabajador = document.getElementById('formTrabajador');
    const btnCancelar = document.getElementById('btnCancelar');
    const tablaTrabajadores = document.getElementById('trabajadoresLista');
    const formTrabajador = document.getElementById('formTrabajador');
    const modalConfirmar = document.getElementById('modalConfirmar');
    const btnCancelarModal = document.getElementById('btnCancelarModal');
    const btnConfirmarModal = document.getElementById('btnConfirmarModal');
    const mensajeConfirmacion = document.getElementById('mensajeConfirmacion');

    // Lista de trabajadores
    let trabajadores = [];

    // Variables de estado
    let trabajadorActualId = null;
    let accionActual = ''; 


    // Funciones auxiliares
    function formatearFecha(fecha) {
        if (!fecha) return '';
        const fechaObj = new Date(fecha);
        if (isNaN(fechaObj.getTime())) return ''; // Fecha inválida
        
        const dia = String(fechaObj.getDate()).padStart(2, '0');
        const mes = String(fechaObj.getMonth() + 1).padStart(2, '0');
        const anio = fechaObj.getFullYear();
        
        return `${dia}/${mes}/${anio}`;
    }
    
    function formatearFechaParaInput(fecha) {
        if (!fecha) return '';
        const fechaObj = new Date(fecha);
        if (isNaN(fechaObj.getTime())) return ''; // Fecha inválida
        
        const dia = String(fechaObj.getDate()).padStart(2, '0');
        const mes = String(fechaObj.getMonth() + 1).padStart(2, '0');
        const anio = fechaObj.getFullYear();
        
        return `${anio}-${mes}-${dia}`;
    }

    function mostrarFormulario(mostrar = true) {
        if (mostrar) {
            document.body.style.overflow = 'hidden'; // Prevenir el scroll del body
            contenedorFormulario.classList.add('mostrar');
            // Enfocar el primer campo del formulario
            setTimeout(() => {
                const primerCampo = contenedorFormulario.querySelector('input:not([type="hidden"])');
                if (primerCampo) primerCampo.focus();
            }, 100);
        } else {
            document.body.style.overflow = ''; // Restaurar el scroll del body
            contenedorFormulario.classList.remove('mostrar');
        }
    }

    function limpiarFormulario() {
        formTrabajador.reset();
        trabajadorActualId = null;
        document.getElementById('tituloFormulario').textContent = 'Nuevo Trabajador';
    }

    function cargarTrabajadores() {
        // Carga simulada
        if (trabajadores.length === 0) {
            tablaTrabajadores.innerHTML = `
                <tr>
                    <td colspan="7" class="sin-datos">No hay trabajadores registrados</td>
                </tr>`;
            return;
        }

        let html = '';
        trabajadores.forEach(trabajador => {
            const fechaFormateada = formatearFecha(trabajador.fechaAcceso);
            const estado = trabajador.activo ? 
                '<span class="estado-activo">Activo</span>' : 
                '<span class="estado-inactivo">Inactivo</span>';
            
            html += `
                <tr>
                    <td>${trabajador.dni}</td>
                    <td>${trabajador.nombre}</td>
                    <td>${trabajador.apellido}</td>
                    <td>${trabajador.telefono}</td>
                    <td>${fechaFormateada}</td>
                    <td>${estado}</td>
                    <td class="acciones-celda">
                        <button class="boton-accion-tabla editar" data-id="${trabajador.id}" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="boton-accion-tabla ${trabajador.activo ? 'deshabilitar' : 'habilitar'}" 
                                data-id="${trabajador.id}" 
                                title="${trabajador.activo ? 'Deshabilitar' : 'Habilitar'}">
                            <i class="fas ${trabajador.activo ? 'fa-user-slash' : 'fa-user-check'}"></i>
                        </button>
                        <button class="boton-accion-tabla" data-id="${trabajador.id}" title="Modificar fecha">
                            <i class="far fa-calendar-alt"></i>
                        </button>
                    </td>
                </tr>`;
        });

        tablaTrabajadores.innerHTML = html;
        
        // Event listeners en los botones
        document.querySelectorAll('.editar').forEach(btn => {
            btn.addEventListener('click', editarTrabajador);
        });
        
        document.querySelectorAll('.boton-accion-tabla:not(.editar)').forEach(btn => {
            btn.addEventListener('click', manejarAccionTrabajador);
        });
    }

    function mostrarModalConfirmacion(mensaje, callback) {
        mensajeConfirmacion.textContent = mensaje;
        modalConfirmar.classList.add('mostrar');
        
        const manejarConfirmacion = (confirmado) => {
            modalConfirmar.classList.remove('mostrar');
            if (confirmado && typeof callback === 'function') {
                callback();
            }
            btnConfirmarModal.removeEventListener('click', confirmar);
            btnCancelarModal.removeEventListener('click', cancelar);
        };
        
        const confirmar = () => manejarConfirmacion(true);
        const cancelar = () => manejarConfirmacion(false);
        
        btnConfirmarModal.addEventListener('click', confirmar);
        btnCancelarModal.addEventListener('click', cancelar);
    }

    // Manejadores de eventos
    function manejarAccionTrabajador(e) {
        const boton = e.currentTarget;
        const id = parseInt(boton.getAttribute('data-id'));
        const esDeshabilitar = boton.classList.contains('deshabilitar');
        const esHabilitar = boton.classList.contains('habilitar');
        const esModificarFecha = boton.querySelector('.fa-calendar-alt');
        
        const trabajador = trabajadores.find(t => t.id === id);
        if (!trabajador) return;
        
        if (esDeshabilitar) {
            mostrarModalConfirmacion(
                `¿Está seguro de que desea deshabilitar a ${trabajador.nombre} ${trabajador.apellido}?`,
                () => {
                    trabajador.activo = false;
                    cargarTrabajadores();
                    alert('Trabajador deshabilitado correctamente');
                }
            );
        } else if (esHabilitar) {
            trabajador.activo = true;
            cargarTrabajadores();
            alert('Trabajador habilitado correctamente');
        } else if (esModificarFecha) {
            const fechaActual = trabajador.fechaAcceso ? 
                formatearFecha(trabajador.fechaAcceso) : 
                new Date().toLocaleDateString('es-ES');
                
            const nuevaFecha = prompt('Ingrese la nueva fecha de acceso (DD/MM/YYYY):', fechaActual);
            
            if (nuevaFecha) {
                if (!/^\d{2}\/\d{2}\/\d{4}$/.test(nuevaFecha)) {
                    alert('Formato de fecha inválido. Use el formato DD/MM/YYYY');
                    return;
                }
                
                const [dia, mes, anio] = nuevaFecha.split('/');
                const fechaFormateada = `${anio}-${mes.padStart(2, '0')}-${dia.padStart(2, '0')}`;
                
                const fechaObj = new Date(fechaFormateada);
                if (isNaN(fechaObj.getTime())) {
                    alert('Fecha inválida');
                    return;
                }
                
                // Valida que la fecha no sea anterior al día actual
                const hoy = new Date();
                hoy.setHours(0, 0, 0, 0);
                if (fechaObj < hoy) {
                    alert('La fecha no puede ser anterior al día actual');
                    return;
                }
                
                trabajador.fechaAcceso = fechaFormateada;
                cargarTrabajadores();
                alert('Fecha de acceso actualizada correctamente');
            }
        }
    }

    function editarTrabajador(e) {
        const id = parseInt(e.currentTarget.getAttribute('data-id'));
        const trabajador = trabajadores.find(t => t.id === id);
        
        if (!trabajador) return;
        
        // Rellena el formulario con los datos del trabajador
        document.getElementById('trabajadorId').value = trabajador.id;
        document.getElementById('dni').value = trabajador.dni;
        document.getElementById('nombre').value = trabajador.nombre;
        document.getElementById('apellido').value = trabajador.apellido;
        document.getElementById('telefono').value = trabajador.telefono;
        document.getElementById('fechaAcceso').value = formatearFechaParaInput(trabajador.fechaAcceso);
        
        // Actualiza el título del formulario
        document.getElementById('tituloFormulario').textContent = 'Editar Trabajador';
        
        // Muestra el formulario
        mostrarFormulario(true);
        
        trabajadorActualId = id;
        accionActual = 'editar';
        
        document.getElementById('dni').focus();
    }

    // Cierra el modal al hacer clic fuera del formulario
    contenedorFormulario.addEventListener('click', function(e) {
        if (e.target === contenedorFormulario) {
            mostrarFormulario(false);
            limpiarFormulario();
        }
    });

    // Cierra el modal con la tecla Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && contenedorFormulario.classList.contains('mostrar')) {
            mostrarFormulario(false);
            limpiarFormulario();
        }
    });

    // Event Listeners
    btnAgregarTrabajador.addEventListener('click', function(e) {
        e.preventDefault();
        limpiarFormulario();
        accionActual = 'agregar';
        mostrarFormulario(true);
        // Establece la fecha mínima como hoy
        const hoy = new Date().toISOString().split('T')[0];
        document.getElementById('fechaAcceso').min = hoy;
    });

    btnCancelar.addEventListener('click', function() {
        mostrarFormulario(false);
        limpiarFormulario();
    });

    formTrabajador.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Obtiene los valores del formulario
        const datos = {
            id: trabajadorActualId || Date.now(), 
            dni: document.getElementById('dni').value.trim(),
            nombre: document.getElementById('nombre').value.trim(),
            apellido: document.getElementById('apellido').value.trim(),
            telefono: document.getElementById('telefono').value.trim(),
            fechaAcceso: document.getElementById('fechaAcceso').value,
            activo: true
        };
        
        // Validaciones básicas
        if (!datos.dni || !datos.nombre || !datos.apellido || !datos.telefono || !datos.fechaAcceso) {
            alert('Todos los campos son obligatorios');
            return;
        }
        
        if (!/^[0-9]{8}$/.test(datos.dni)) {
            alert('El DNI debe tener exactamente 8 dígitos');
            return;
        }
        
        if (!/^[0-9]{9}$/.test(datos.telefono)) {
            alert('El teléfono debe tener exactamente 9 dígitos');
            return;
        }
        
        if (accionActual === 'agregar') {
            if (trabajadores.some(t => t.dni === datos.dni)) {
                alert('Ya existe un trabajador con este DNI');
                return;
            }
            trabajadores.push(datos);
            alert('Trabajador agregado correctamente');
        } else {
            // Actualiza los datos del trabajador existente
            const indice = trabajadores.findIndex(t => t.id === trabajadorActualId);
            if (indice !== -1) {
                // Mantener el estado actual del trabajador
                datos.activo = trabajadores[indice].activo;
                trabajadores[indice] = datos;
            }
        }
        
        // Recargar la lista y limpiar el formulario
        cargarTrabajadores();
        mostrarFormulario(false);
        limpiarFormulario();
    });

    // Cargar los trabajadores al iniciar
    cargarTrabajadores();
});
