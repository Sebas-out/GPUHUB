console.log('=== Cargando inventario-nuevo.js ===');

// Configuración global para fetch
const fetchConfig = {
    credentials: 'include', // Incluir cookies en todas las peticiones
    headers: {
        'Content-Type': 'application/json'
    }
};

// Variables globales
const almacenes = [];
const productos = [];
let almacenSeleccionado = null;

// Funciones de utilidad
function mostrarError(mensaje) {
    console.error(mensaje);
    alert(mensaje);
}

// Funciones globales
window.seleccionarAlmacen = async function(id) {
    almacenSeleccionado = id;
    const almacen = almacenes.find(a => a.id_almacen === id);
    
    if (almacen) {
        try {
            // Actualizar la UI para mostrar que se está cargando
            const listaProductos = document.getElementById('listaProductos');
            if (listaProductos) {
                listaProductos.innerHTML = `
                    <div class="col-12 text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando...</span>
                        </div>
                        <p class="mt-2">Cargando productos...</p>
                    </div>`;
            }
            
            // Obtener productos del almacén desde la API
            const response = await fetch(`/api/almacenes/${id}/productos`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                credentials: 'include' // Importante para incluir las cookies de autenticación
            });
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Error al cargar productos: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Actualizar la lista de productos
            productos.length = 0;
            if (Array.isArray(data)) {
                // Mantener la estructura original del producto sin transformarla
                productos.push(...data);
            }
            
            renderizarProductos();
            
            // Actualizar el título con el nombre del almacén
            const tituloInventario = document.getElementById('tituloInventario');
            if (tituloInventario) {
                tituloInventario.textContent = `Inventario - ${almacen.nombre}`;
            }
            
            // Habilitar el botón de agregar producto
            const btnAgregarProducto = document.getElementById('btnAgregarProducto');
            if (btnAgregarProducto) {
                btnAgregarProducto.disabled = false;
            }
            
        } catch (error) {
            console.error('Error al cargar los productos:', error);
            mostrarError('No se pudieron cargar los productos del almacén: ' + error.message);
        }
    }
};

function cargarProductos(almacenId) {
    console.log('Cargando productos para almacén:', almacenId);
    fetch(`/api/productos/almacen/${almacenId}`)
        .then(response => {
            console.log('Respuesta de la API:', response);
            if (!response.ok) {
                throw new Error('Error al cargar productos: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Datos de productos recibidos:', data);
            productos = data;
            renderizarProductos();
        })
        .catch(error => {
            console.error('Error al cargar productos:', error);
            alert('Error al cargar los productos: ' + error.message);
        });
}

window.editarAlmacen = function(id) {
    const almacen = almacenes.find(a => a.id_almacen === id);
    if (almacen) {
        mostrarFormularioAlmacen(almacen);
    }
};

// Variables globales para el modal de confirmación de eliminación de almacén
let almacenAEliminarId = null;

// Función para mostrar el modal de confirmación de eliminación de almacén
async function mostrarModalEliminarAlmacen(id) {
    almacenAEliminarId = id;
    
    const mensajeProductos = document.getElementById('mensajeProductosAlmacen');
    
    try {
        // Obtener el número de productos en el almacén
        const response = await fetch(`/api/almacenes/${id}/productos/count`, {
            credentials: 'include'
        });
        
        if (response.ok) {
            const data = await response.json();
            const count = data.count || 0;
            
            if (count > 0) {
                mensajeProductos.textContent = `¡Atención! Se eliminarán ${count} productos asociados a este almacén.`;
                mensajeProductos.style.display = 'block';
            } else {
                mensajeProductos.style.display = 'none';
            }
        } else {
            console.error('Error al obtener el número de productos:', response.status);
            mensajeProductos.style.display = 'none';
        }
    } catch (error) {
        console.error('Error al obtener el número de productos:', error);
        mensajeProductos.style.display = 'none';
    }
    
    // Mostrar el modal
    const modal = new bootstrap.Modal(document.getElementById('confirmarEliminarAlmacenModal'));
    modal.show();
}

// Configurar el manejador del botón de confirmación de eliminación de almacén
document.addEventListener('DOMContentLoaded', function() {
    const confirmarEliminarAlmacenBtn = document.getElementById('confirmarEliminarAlmacenBtn');
    if (confirmarEliminarAlmacenBtn) {
        confirmarEliminarAlmacenBtn.addEventListener('click', async function() {
            if (almacenAEliminarId !== null) {
                await procederConEliminacionAlmacen(almacenAEliminarId);
                
                // Cerrar el modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('confirmarEliminarAlmacenModal'));
                if (modal) {
                    modal.hide();
                }
                
                // Limpiar la variable
                almacenAEliminarId = null;
            }
        });
    }
});

// Función para proceder con la eliminación del almacén
async function procederConEliminacionAlmacen(id) {
    try {
        const response = await fetch(`/api/almacenes/${id}`, { 
            method: 'DELETE',
            credentials: 'include' // Importante para incluir las cookies de autenticación
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al eliminar el almacén');
        }
        
        // Recargar la lista de almacenes
        await cargarAlmacenes();
        
        // Si el almacén eliminado era el seleccionado, limpiar la vista de productos
        if (almacenSeleccionado === id) {
            almacenSeleccionado = null;
            productos.length = 0; // Limpiar array de productos
            renderizarProductos();
            
            // Actualizar el título
            const tituloInventario = document.getElementById('tituloInventario');
            if (tituloInventario) {
                tituloInventario.textContent = 'Inventario';
            }
            
            // Deshabilitar el botón de agregar producto
            const btnAgregarProducto = document.getElementById('btnAgregarProducto');
            if (btnAgregarProducto) {
                btnAgregarProducto.disabled = true;
            }
        }
        
        // Mostrar mensaje de éxito
        // mostrarExito('Almacén eliminado correctamente');
        
    } catch (error) {
        console.error('Error al eliminar el almacén:', error);
        mostrarError('No se pudo eliminar el almacén: ' + error.message);
    }
}

// Función para mostrar un mensaje de éxito
function mostrarExito(mensaje) {
    // Puedes implementar un sistema de notificaciones más elegante aquí
    alert(mensaje);
}

window.eliminarAlmacen = function(id, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
    }
    
    mostrarModalEliminarAlmacen(id);
};

// Función para limpiar el modal y el backdrop
function limpiarModal() {
    const backdrop = document.querySelector('.modal-backdrop');
    if (backdrop) {
        backdrop.remove();
    }
    // Restaurar el scroll del body
    document.body.style.overflow = 'auto';
    document.body.style.paddingRight = '0';
}

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar referencias a elementos del DOM
    const btnAgregarProducto = document.getElementById('btnAgregarProducto');
    const btnAgregarAlmacen = document.getElementById('btnAgregarAlmacen');
    
    // Configurar el manejador para el botón de cierre del modal
    const modalElement = document.getElementById('modalProducto');
    if (modalElement) {
        modalElement.addEventListener('hidden.bs.modal', function() {
            limpiarModal();
        });
        
        // Manejar el cierre con la tecla Escape
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape' && modalElement.classList.contains('show')) {
                limpiarModal();
            }
        });
    }
    const btnCancelarAlmacen = document.getElementById('btnCancelarAlmacen');
    const listaProductos = document.getElementById('listaProductos');
    const tituloInventario = document.getElementById('tituloInventario');
    
    // Configurar el botón de agregar producto
    if (btnAgregarProducto) {
        btnAgregarProducto.disabled = true;
        btnAgregarProducto.addEventListener('click', function(e) {
            e.preventDefault();
            if (!almacenSeleccionado) {
                mostrarError('Por favor seleccione un almacén primero');
                return;
            }
            mostrarFormularioProducto();
        });
    }
    
    // El botón de agregar primer producto ha sido eliminado
    
    // Configurar el botón de agregar almacén
    if (btnAgregarAlmacen) {
        btnAgregarAlmacen.addEventListener('click', function(e) {
            e.preventDefault();
            mostrarFormularioAlmacen();
        });
    }
    
    // Configurar el botón de cancelar en el modal de almacén
    if (btnCancelarAlmacen) {
        btnCancelarAlmacen.addEventListener('click', function() {
            const modal = bootstrap.Modal.getInstance(document.getElementById('modalAlmacen'));
            if (modal) modal.hide();
        });
    }
    
    // Cargar los almacenes al inicio
    cargarAlmacenes();
    
    // Configurar el formulario de almacén
    const formAlmacen = document.getElementById('formAlmacen');
    if (formAlmacen) {
        formAlmacen.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const almacenId = document.getElementById('almacenId').value;
            const url = almacenId ? `/api/almacenes/${almacenId}` : '/api/almacenes';
            const method = almacenId ? 'PUT' : 'POST';
            
            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify({
                        nombre: formData.get('nombre'),
                        direccion: formData.get('direccion')
                    })
                });
                
                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    throw new Error(errorData.message || 'Error al guardar el almacén');
                }
                
                // Cerrar el modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('modalAlmacen'));
                if (modal) modal.hide();
                
                // Recargar la lista de almacenes
                await cargarAlmacenes();
                
            } catch (error) {
                console.error('Error al guardar el almacén:', error);
                // alert('No se pudo guardar el almacén: ' + error.message);
            }
        });
    }
    
    // Configurar el formulario de producto
    const formProducto = document.getElementById('formProducto');
    const imagenInput = document.getElementById('imagenProducto');
    const imagenPreview = document.getElementById('vistaPreviaImagen');
    const nombreArchivo = document.getElementById('nombreArchivo');
    
    // Configurar la vista previa de la imagen
    if (imagenInput) {
        imagenInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                // Mostrar el nombre del archivo
                nombreArchivo.textContent = file.name;
                
                // Crear una vista previa de la imagen
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagenPreview.innerHTML = `
                        <div class="mt-2">
                            <img src="${e.target.result}" class="img-thumbnail" style="max-height: 200px;">
                        </div>
                    `;
                };
                reader.readAsDataURL(file);
            } else {
                nombreArchivo.textContent = 'Ningún archivo seleccionado';
                imagenPreview.innerHTML = '';
            }
        });
    }
    
    if (formProducto) {
        formProducto.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            // Obtener el ID del producto del campo oculto o del formulario
            const productoIdInput = document.querySelector('input[name="id"]');
            const productoId = productoIdInput ? productoIdInput.value : '';
            const almacenId = document.getElementById('almacenIdProducto').value;
            const marcaEnsambladora = document.getElementById('marcaEnsambladora')?.value.trim();
            const marcaTarjeta = document.getElementById('marcaTarjeta')?.value.trim();
            const modelo = document.getElementById('modelo')?.value.trim();
            const numeroSerie = document.getElementById('serie')?.value.trim();
            const descripcion = document.getElementById('descripcion')?.value.trim();
            const estadoStock = document.querySelector('input[name="estadoStock"]:checked')?.value || 'disponible';
            
            // Obtener especificaciones técnicas (tipo 1)
            const especificaciones = [];
            const especificacionesElements = document.querySelectorAll('#especificaciones > .d-flex');
            especificacionesElements.forEach(espec => {
                const inputs = espec.querySelectorAll('input[type="text"]');
                if (inputs.length >= 2 && inputs[0].value.trim() && inputs[1].value.trim()) {
                    const obj = {
                        tipo: 1,
                        descripcion: inputs[0].value.trim(),
                        valor: inputs[1].value.trim()
                    };
                    // Leer id_datos si existe
                    if (espec.dataset.idDatos) obj.id_datos = parseInt(espec.dataset.idDatos);
                    especificaciones.push(obj);
                }
            });
            
            // Obtener componentes compatibles (tipo 0)
            const componentes = [];
            const componentesElements = document.querySelectorAll('#componentes > .d-flex');
            componentesElements.forEach(comp => {
                const input = comp.querySelector('input[type="text"]');
                if (input && input.value.trim()) {
                    const obj = {
                        tipo: 0,
                        descripcion: input.value.trim(),
                        valor: null
                    };
                    // Leer id_datos si existe
                    if (comp.dataset.idDatos) obj.id_datos = parseInt(comp.dataset.idDatos);
                    componentes.push(obj);
                }
            });
            
            if (!marcaEnsambladora || !marcaTarjeta || !modelo || !numeroSerie) {
                mostrarError('Por favor complete todos los campos obligatorios');
                return;
            }
            
            try {
                const url = productoId ? `/api/productos/${productoId}` : '/api/productos';
                const method = productoId ? 'PUT' : 'POST';
                
                // Validar que se haya seleccionado un almacén
                const almacenId = document.getElementById('almacenIdProducto')?.value;
                if (!almacenId) {
                    mostrarError('Por favor seleccione un almacén');
                    return;
                }
                
                // Crear el objeto FormData para manejar la carga de archivos
                const formData = new FormData();
                
                // Agregar los campos del formulario al FormData
                formData.append('idAlmacen', almacenId);
                formData.append('marcaEnsambladora', marcaEnsambladora);
                formData.append('marcaTarjeta', marcaTarjeta);
                formData.append('modelo', modelo);
                formData.append('numeroSerie', numeroSerie);
                formData.append('descripcion', descripcion || '');
                formData.append('estado', estadoStock);
                
                // Agregar la imagen si existe
                const imagenInput = document.getElementById('imagenProducto');
                if (imagenInput.files.length > 0) {
                    formData.append('imagen', imagenInput.files[0]);
                }
                
                // Agregar especificaciones y componentes como JSON
                formData.append('especificaciones', JSON.stringify(especificaciones));
                formData.append('componentes', JSON.stringify(componentes));
                
                // Si es una edición, agregar el ID del producto
                if (productoId) {
                    formData.append('id', productoId);
                }
                
                // No establecer el encabezado 'Content-Type' cuando se usa FormData
                // El navegador lo establecerá automáticamente con el límite correcto
                const response = await fetch(url, {
                    method: method,
                    credentials: 'include',
                    body: formData
                });
                
                console.log('Respuesta de la API:', response);
                
                // Leer la respuesta como texto primero
                const responseText = await response.text();
                let responseData;
                
                try {
                    // Intentar analizar el texto como JSON
                    responseData = responseText ? JSON.parse(responseText) : {};
                    console.log('Datos de productos recibidos:', responseData);
                } catch (e) {
                    console.error('Error al analizar la respuesta JSON:', responseText);
                    throw new Error(`Error en la respuesta del servidor: ${responseText || 'Respuesta vacía'}`);
                }
                
                if (!response.ok) {
                    throw new Error(responseData.message || `Error al guardar el producto: ${response.status} ${response.statusText}`);
                }
                
                // Recargar los productos del almacén actual
                if (almacenSeleccionado) {
                    await seleccionarAlmacen(almacenSeleccionado);
                }
                
                // Cerrar el modal sin mostrar mensaje de éxito
                const modalElement = document.getElementById('modalProducto');
                if (modalElement) {
                    const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
                    
                    // Configurar el manejador para cuando el modal se oculte
                    const handleHidden = () => {
                        // Eliminar el manejador de eventos para evitar duplicados
                        modalElement.removeEventListener('hidden.bs.modal', handleHidden);
                        
                        // Limpiar el formulario
                        const formProducto = document.getElementById('formProducto');
                        if (formProducto) {
                            formProducto.reset();
                            
                            // Limpiar el campo de imagen
                            const imagenInput = document.getElementById('imagenProducto');
                            if (imagenInput) {
                                imagenInput.value = '';
                            }
                            
                            // Limpiar el nombre del archivo
                            const nombreArchivo = document.getElementById('nombreArchivo');
                            if (nombreArchivo) {
                                nombreArchivo.textContent = 'Ningún archivo seleccionado';
                            }
                            
                            // Limpiar la vista previa de la imagen
                            const imagenPreview = document.getElementById('imagenPreview');
                            if (imagenPreview) {
                                imagenPreview.src = '#';
                                imagenPreview.style.display = 'none';
                            }
                            
                            // Limpiar el ID del producto
                            const productoIdInput = document.querySelector('input[name="id"]');
                            if (productoIdInput) {
                                productoIdInput.remove();
                            }
                        }
                        
                        // Forzar la eliminación del backdrop si aún existe
                        const backdrop = document.querySelector('.modal-backdrop');
                        if (backdrop) {
                            backdrop.remove();
                        }
                        // Habilitar el scroll del body si se deshabilitó
                        document.body.style.overflow = 'auto';
                        document.body.style.paddingRight = '0';
                    };
                    
                    // Agregar el manejador de eventos
                    modalElement.addEventListener('hidden.bs.modal', handleHidden);
                    
                    // Ocultar el modal
                    modal.hide();
                }
                
            } catch (error) {
                console.error('Error al guardar el producto:', error);
                mostrarError('No se pudo guardar el producto: ' + error.message);
            }
        });
    }
});

// Función para cargar los almacenes
async function cargarAlmacenes() {
    try {
        // Mostrar estado de carga
        const listaProductos = document.getElementById('listaProductos');
        if (listaProductos) {
            listaProductos.innerHTML = `
                <div class="col-12 text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Cargando...</span>
                    </div>
                    <p class="mt-2">Cargando almacenes...</p>
                </div>`;
        }
        
        // Limpiar array existente
        almacenes.length = 0;
        
        // Obtener almacenes desde la API
        const response = await fetch('/api/almacenes', {
            credentials: 'include' // Importante para incluir las cookies de autenticación
        });
        
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('No tiene permisos para ver los almacenes');
            } else {
                throw new Error(`Error HTTP: ${response.status}`);
            }
        }
        
        const data = await response.json();
        
        // Mapear los datos de la API al formato esperado
        data.forEach(almacen => {
            almacenes.push({
                id_almacen: almacen.id_almacen,
                nombre: almacen.nombre,
                direccion: almacen.direccion,
                productos: almacen.productos || []  // Incluir los productos del almacén
            });
        });
        
        renderizarAlmacenes();
        
    } catch (error) {
        console.error('Error al cargar almacenes:', error);
        mostrarError('No se pudieron cargar los almacenes: ' + error.message);
    }
}

// Función para renderizar la lista de almacenes
function renderizarAlmacenes() {
    const listaAlmacenes = document.getElementById('listaAlmacenes');
    if (!listaAlmacenes) return;
    
    if (almacenes.length === 0) {
        listaAlmacenes.innerHTML = `
            <div class="col-12 text-center py-5">
                <p class="text-muted">No hay almacenes disponibles</p>
            </div>`;
        return;
    }
    
    const html = almacenes.map(almacen => `
        <div class="list-group-item list-group-item-action ${almacen.id_almacen === almacenSeleccionado ? 'active' : ''}"
             onclick="seleccionarAlmacen(${almacen.id_almacen})">
            <div class="d-flex w-100 justify-content-between">
                <h5 class="mb-1">${almacen.nombre || 'Sin nombre'}</h5>
                <small>ID: ${almacen.id_almacen}</small>
            </div>
            <p class="mb-1">${almacen.direccion || 'Sin dirección'}</p>
            <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary me-2" 
                        onclick="event.stopPropagation(); editarAlmacen(${almacen.id_almacen})"
                        title="Editar almacén">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-outline-danger" 
                        onclick="event.stopPropagation(); eliminarAlmacen(${almacen.id_almacen})"
                        title="Eliminar almacén">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    listaAlmacenes.innerHTML = html;
}

// Función para renderizar la lista de productos
function renderizarProductos() {
    const listaProductos = document.getElementById('listaProductos');
    if (!listaProductos) return;

    if (productos.length === 0) {
        listaProductos.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="text-muted">No hay productos en este almacén</div>
            </div>`;
        return;
    }

    let html = '';
    // Mostrar en consola los productos recibidos para depuración
    console.log('=== PRODUCTOS RECIBIDOS ===');
    console.log('Productos crudos:', JSON.parse(JSON.stringify(productos)));
    
    // Mostrar las claves del primer producto para verificar la estructura
    if (productos.length > 0) {
        console.log('Claves del primer producto:', Object.keys(productos[0]));
    }
    
    // Procesar cada producto
    productos.forEach(producto => {
        // 1. Obtener y limpiar los datos según el DTO
        const datosProducto = {
            id: producto.id_producto,
            marcaEnsambladora: (producto.marcaEnsambladora || '').trim(),
            marcaTarjeta: (producto.marcaTarjeta || '').trim(),
            modelo: (producto.modelo || '').trim(),
            descripcion: (producto.descripcion || '').trim(),
            numeroSerie: (producto.numeroSerie || '').trim(),
            imagen: producto.imagenBase64 || '', // Usar imagenBase64 en lugar de imagenes
            estado: (producto.estado || 'Sin estado').trim()
        };
        
        // 3. Establecer valores por defecto si están vacíos
        if (!datosProducto.modelo) datosProducto.modelo = 'Sin modelo';
        if (!datosProducto.marcaTarjeta) datosProducto.marcaTarjeta = 'Sin marca';
        if (!datosProducto.marcaEnsambladora) datosProducto.marcaEnsambladora = 'Sin ensambladora';
        if (!datosProducto.descripcion) datosProducto.descripcion = 'Sin descripción';
        if (!datosProducto.numeroSerie) datosProducto.numeroSerie = 'N/A';
        
        console.log('Producto procesado - Estructura:', {
            id: datosProducto.id,
            marcaEnsambladora: datosProducto.marcaEnsambladora,
            marcaTarjeta: datosProducto.marcaTarjeta,
            modelo: datosProducto.modelo,
            descripcion: datosProducto.descripcion,
            numeroSerie: datosProducto.numeroSerie,
            tieneImagen: !!datosProducto.imagen,
            tipoImagen: typeof datosProducto.imagen
        });
        
        html += `
            <div class="col-md-4 mb-4">
                <div class="card h-100 producto-card" style="background-color: #1a1a1a; border: 1px solid #ff0; color: white;">
                    ${datosProducto.imagen ? `
                        <img src="${datosProducto.imagen}" 
                             class="card-img-top" 
                             alt="${datosProducto.marcaTarjeta} ${datosProducto.modelo}" 
                             onerror="this.onerror=null; this.src='data:image/svg+xml;charset=UTF-8,%3Csvg width=\'200\' height=\'200\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Crect width=\'100%\' height=\'100%\' fill=\'%23000\'/%3E%3Ctext x=\'50%\' y=\'50%\' font-family=\'sans-serif\' font-size=\'14\' fill=\'%23ff0\' text-anchor=\'middle\' dominant-baseline=\'middle\'%3EImagen no disponible%3C/text%3E%3C/svg%3E'"
                             style="height: 200px; object-fit: contain; background-color: #000; padding: 10px;">
                    ` : `
                        <div class="text-center py-5 bg-dark" style="height: 200px; display: flex; align-items: center; justify-content: center;">
                            <i class="fas fa-image fa-4x text-warning"></i>
                        </div>
                    `}
                    <div class="card-body">
                        <h5 class="card-title text-warning">${datosProducto.modelo}</h5>
                        <p class="card-text">${datosProducto.marcaTarjeta}</p>
                    </div>
                    <ul class="list-group list-group-flush">
                        <li class="list-group-item bg-transparent text-light border-warning">
                            <i class="fas fa-barcode me-2 text-warning"></i> ${datosProducto.marcaEnsambladora}
                            </li>
                            <li class="list-group-item bg-transparent text-light border-warning">
                            <i class="fas fa-barcode me-2 text-warning"></i> ${datosProducto.numeroSerie}
                            </li>
                            <li class="list-group-item bg-transparent text-light border-warning">
                            <i class="fas fa-barcode me-2 text-warning"></i> ${datosProducto.estado}
                            </li>
                        </li>
                    </ul>
                    <div class="card-body">
                        <button class="btn btn-warning btn-sm me-2 btn-editar" data-id="${datosProducto.id}">
                            <i class="fas fa-edit me-1"></i> Editar
                        </button>
                        <button class="btn btn-outline-danger btn-sm btn-eliminar" data-id="${datosProducto.id}">
                            <i class="fas fa-trash me-1"></i> Eliminar
                        </button>
                    </div>
                </div>
            </div>`;
    });

    if (listaProductos) {
        listaProductos.innerHTML = html;
        
        // Configurar manejadores de eventos para los botones usando event delegation
        listaProductos.addEventListener('click', function(e) {
            const deleteBtn = e.target.closest('.btn-eliminar');
            const editBtn = e.target.closest('.btn-editar');
            
            if (deleteBtn) {
                e.preventDefault();
                e.stopPropagation();
                const id = deleteBtn.getAttribute('data-id');
                console.log('Botón eliminar clickeado para ID:', id);
                if (id) {
                    eliminarProducto(id, e);
                }
                return false;
            }
            
            if (editBtn) {
                e.preventDefault();
                e.stopPropagation();
                const id = editBtn.getAttribute('data-id');
                console.log('Botón editar clickeado para ID:', id);
                if (id) {
                    editarProducto(id, e);
                }
                return false;
            }
        });
    }
}

// Función para mostrar el formulario de almacén
function mostrarFormularioAlmacen(almacen = null) {
    const modalElement = document.getElementById('modalAlmacen');
    const modal = new bootstrap.Modal(modalElement);
    const form = document.getElementById('formAlmacen');
    const tituloModal = document.getElementById('modalAlmacenLabel');
    const idInput = document.getElementById('almacenId');
    const nombreInput = document.getElementById('nombreAlmacen');
    const direccionInput = document.getElementById('direccionAlmacen');
    
    // Limpiar el formulario
    form.reset();
    
    if (almacen) {
        // Modo edición
        tituloModal.textContent = 'Editar Almacén';
        idInput.value = almacen.id_almacen || '';
        nombreInput.value = almacen.nombre || '';
        direccionInput.value = almacen.direccion || '';
    } else {
        // Modo creación
        tituloModal.textContent = 'Agregar Almacén';
        idInput.value = '';
    }
    
    // Mostrar el modal
    modal.show();
}

document.addEventListener('DOMContentLoaded', function() {
    const formAlmacen = document.getElementById('formAlmacen');
    if (formAlmacen) {
        const handleSubmit = async (e) => {
            e.preventDefault();
            
            const formData = {
                nombre: document.getElementById('nombreAlmacen').value.trim(),
                direccion: document.getElementById('direccionAlmacen').value.trim()
            };
            
            try {
                const url = document.getElementById('almacenId').value ? `/api/almacenes/${document.getElementById('almacenId').value}` : '/api/almacenes';
                const method = document.getElementById('almacenId').value ? 'PUT' : 'POST';
                
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify(formData)
                });
                
                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    throw new Error(errorData.message || 'Error al guardar el almacén');
                }
                
                // Cerrar el modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('modalAlmacen'));
                if (modal) {
                    modal.hide();
                }
                
                // Recargar la lista de almacenes
                await cargarAlmacenes();
                
            } catch (error) {
                console.error('Error al guardar el almacén:', error);
                alert('No se pudo guardar el almacén: ' + error.message);
            }
        };
        
        // Eliminar cualquier manejador de eventos anterior
        formAlmacen.removeEventListener('submit', handleSubmit);
        formAlmacen.addEventListener('submit', handleSubmit);
    }
});

async function editarProducto(id) {
    try {
        const response = await fetch(`/api/productos/${id}`);
        if (!response.ok) {
            throw new Error('Error al cargar el producto');
        }
        
        const producto = await response.json();
        console.log('Producto cargado para edición:', producto);
        
        // Mostrar el formulario con los datos del producto
        // La función mostrarFormularioProducto se encargará de configurar todos los campos
        mostrarFormularioProducto(producto);
        
        // Configurar eventos de los botones después de cargar los datos
        configurarEventosFormulario();
        
    } catch (error) {
        console.error('Error al cargar el producto:', error);
        mostrarError('Error al cargar el producto: ' + error.message);
    }
}

// Variables globales para el modal de confirmación
let productoAEliminarId = null;
let productoAEliminarEvent = null;

// Función para mostrar el modal de confirmación
function mostrarModalConfirmacion(id, event) {
    productoAEliminarId = id;
    productoAEliminarEvent = event;
    
    const modal = new bootstrap.Modal(document.getElementById('confirmarEliminarModal'));
    modal.show();
}

// Función para manejar la confirmación de eliminación
document.addEventListener('DOMContentLoaded', function() {
    const confirmarEliminarBtn = document.getElementById('confirmarEliminarBtn');
    if (confirmarEliminarBtn) {
        confirmarEliminarBtn.addEventListener('click', async function() {
            if (productoAEliminarId) {
                console.log('Usuario confirmó la eliminación');
                await procederConEliminacion(productoAEliminarId, productoAEliminarEvent);
                
                // Cerrar el modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('confirmarEliminarModal'));
                if (modal) {
                    modal.hide();
                }
                
                // Limpiar las variables
                productoAEliminarId = null;
                productoAEliminarEvent = null;
            }
        });
    }
});

// Función para eliminar un producto - Versión mejorada con mejor manejo de errores
window.eliminarProducto = async function(id, event = null) {
    console.log('=== INICIO eliminarProducto ===');
    console.log('ID del producto a eliminar:', id);
    
    // Detener cualquier evento inmediatamente
    if (event) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
    }
    
    // Mostrar el modal de confirmación
    mostrarModalConfirmacion(id, event);
};

// Función separada para manejar la eliminación después de la confirmación
async function procederConEliminacion(id, event) {
    console.log('Iniciando eliminación del producto ID:', id);
    
    // Actualizar el botón
    const boton = event ? event.target.closest('button') : null;
    if (boton) {
        const originalHTML = boton.innerHTML;
        boton.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Eliminando...';
        boton.disabled = true;
    }
    
    try {
        
        // Este código ahora está en la función procederConEliminacion
        
        // 4. Hacer la petición DELETE
        console.log('Realizando petición DELETE a:', `/api/productos/${id}`);
        
        const response = await fetch(`/api/productos/${id}`, {
            method: 'DELETE',
            credentials: 'include',  // Importante para enviar las cookies de sesión
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'  // Ayuda a identificar peticiones AJAX
            }
        });
        
        console.log('Respuesta recibida. Estado:', response.status);
        
        const responseData = await response.json().catch(() => ({}));
        console.log('Contenido de la respuesta:', responseData);
        
        if (!response.ok) {
            const errorMessage = responseData.message || 'Error desconocido al eliminar el producto';
            console.error('Error en la respuesta:', {
                status: response.status,
                statusText: response.statusText,
                error: errorMessage,
                response: responseData
            });
            throw new Error(errorMessage);
        }
        
        // 5. Mostrar mensaje de éxito
        // const successMessage = responseData.message || 'Producto eliminado correctamente';
        // console.log('Éxito:', successMessage);
        // alert(successMessage);
        
        // 6. Recargar la página para ver los cambios
        window.location.reload();
        
    } catch (error) {
        console.error('Error en eliminarProducto:', {
            name: error.name,
            message: error.message,
            stack: error.stack
        });
        
        // Mostrar mensaje de error detallado
        const errorMessage = error.message || 'Error desconocido al intentar eliminar el producto';
        alert(`Error al eliminar el producto: ${errorMessage}`);
        
        // Reactivar el botón en caso de error
        const boton = event ? event.target.closest('a') : null;
        if (boton) {
            boton.innerHTML = '<i class="fas fa-trash"></i>';
            boton.disabled = false;
        }
    }
};

// Función para mostrar el formulario de producto
function mostrarFormularioProducto(producto = null) {
    console.log('Mostrando formulario para producto:', producto);
    if (!almacenSeleccionado && !producto) {
        mostrarError('Por favor seleccione un almacén primero');
        return;
    }
    
    // Configurar el formulario con los datos del producto si existe
    if (producto) {
        // Establecer valores básicos del producto
        const form = document.getElementById('formProducto');
        if (form) {
            // Limpiar cualquier campo oculto de ID existente
            const existingIdInput = form.querySelector('input[name="id"]');
            if (existingIdInput) {
                form.removeChild(existingIdInput);
            }
            
            // Llenar campos básicos
            const campos = ['marcaEnsambladora', 'marcaTarjeta', 'modelo', 'descripcion', 'numeroSerie', 'estado'];
            campos.forEach(campo => {
                const input = form.querySelector(`[name="${campo}"]`);
                if (input && producto[campo] !== undefined) {
                    input.value = producto[campo] || '';
                }
            });
            
            // Configurar el ID del producto en un campo oculto
            if (producto.id_producto) {
                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'id';
                idInput.value = producto.id_producto;
                form.appendChild(idInput);
            }
            
            // Configurar el almacén seleccionado
            const almacenSelect = document.getElementById('idAlmacen');
            if (almacenSelect && producto.idAlmacen) {
                almacenSelect.value = producto.idAlmacen;
            }
            
            // Configurar la vista previa de la imagen si existe
            const imagenPreview = document.getElementById('imagenPreview');
            if (imagenPreview) {
                if (producto.imagenBase64) {
                    imagenPreview.src = producto.imagenBase64;
                    imagenPreview.style.display = 'block';
                } else {
                    imagenPreview.src = '#';
                    imagenPreview.style.display = 'none';
                }
            }
        }
    }
    
    // Limpiar el formulario
    const formProducto = document.getElementById('formProducto');
    const imagenPreview = document.getElementById('vistaPreviaImagen');
    const nombreArchivo = document.getElementById('nombreArchivo');
    
    if (formProducto) {
        formProducto.reset();
        
        // Limpiar vista previa de imagen
        if (imagenPreview) {
            imagenPreview.innerHTML = '';
        }
        
        if (nombreArchivo) {
            nombreArchivo.textContent = 'Ningún archivo seleccionado';
        }
        
        // Establecer valores por defecto o del producto a editar
        if (producto) {
            document.getElementById('productoId').value = producto.id || '';
            document.getElementById('almacenIdProducto').value = producto.almacenId || almacenSeleccionado;
            document.getElementById('marcaEnsambladora').value = producto.marcaEnsambladora || '';
            document.getElementById('marcaTarjeta').value = producto.marcaTarjeta || '';
            document.getElementById('modelo').value = producto.modelo || '';
            document.getElementById('serie').value = producto.numeroSerie || '';
            document.getElementById('descripcion').value = producto.descripcion || '';
            
            // Establecer el estado del stock
            if (producto.estado) {
                const radioBtn = document.querySelector(`input[name="estadoStock"][value="${producto.estado}"]`);
                if (radioBtn) {
                    radioBtn.checked = true;
                }
            }
            
            // Mostrar vista previa de la imagen si existe
            if (producto.imagenes) {
                if (imagenPreview) {
                    imagenPreview.innerHTML = `
                        <div class="mt-2">
                            <img src="${producto.imagenes}" class="img-thumbnail" style="max-height: 200px; width: 100%; object-fit: contain;">
                        </div>
                    `;
                }
                if (nombreArchivo) {
                    nombreArchivo.textContent = 'Imagen actual';
                }
            }
            
            // Establecer precio y stock si existen
            if (producto.precio) {
                document.getElementById('precio').value = producto.precio;
            }
            if (producto.stock) {
                document.getElementById('stock').value = producto.stock;
            }
        } else {
            // Valores por defecto para nuevo producto
            document.getElementById('productoId').value = '';
            document.getElementById('almacenIdProducto').value = almacenSeleccionado;
            // Establecer estado por defecto a 'disponible'
            const radioBtn = document.querySelector('input[name="estadoStock"][value="disponible"]');
            if (radioBtn) {
                radioBtn.checked = true;
            }
        }
        
        // Limpiar contenedores dinámicos
        const especificacionesContainer = document.getElementById('especificaciones');
        const componentesContainer = document.getElementById('componentes');
        
        if (especificacionesContainer) {
            especificacionesContainer.innerHTML = `
                <div class="d-flex mb-2">
                    <input type="text" placeholder="Característica" class="form-control me-2" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <input type="text" placeholder="Valor" class="form-control me-2" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                </div>`;
                
            // Si hay un producto, cargar sus especificaciones
            if (producto && producto.especificaciones) {
                // Implementar lógica para cargar especificaciones existentes
                especificacionesContainer.innerHTML = '';
                producto.especificaciones.forEach(espec => {
                    const div = document.createElement('div');
                    div.className = 'd-flex mb-2';
                    // Agregar id_datos como data attribute
                    if (espec.id_datos) div.dataset.idDatos = espec.id_datos;
                    div.innerHTML = `
                        <input type="text" placeholder="Característica" class="form-control me-2" 
                               style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;"
                               value="${espec.descripcion || ''}">
                        <input type="text" placeholder="Valor" class="form-control me-2" 
                               style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;"
                               value="${espec.valor || ''}">
                        <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    especificacionesContainer.appendChild(div);
                });
                
                // Agregar un campo vacío para nueva especificación
                const div = document.createElement('div');
                div.className = 'd-flex mb-2';
                div.innerHTML = `
                    <input type="text" placeholder="Característica" class="form-control me-2" 
                           style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <input type="text" placeholder="Valor" class="form-control me-2" 
                           style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                especificacionesContainer.appendChild(div);
            }
        }
        
        if (componentesContainer) {
            componentesContainer.innerHTML = `
                <div class="d-flex mb-2">
                    <input type="text" placeholder="Componente" class="form-control me-2" 
                           style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                </div>`;
                
            // Si hay un producto, cargar sus componentes
            if (producto && producto.componentes) {
                // Implementar lógica para cargar componentes existentes
                componentesContainer.innerHTML = '';
                producto.componentes.forEach(comp => {
                    const div = document.createElement('div');
                    div.className = 'd-flex mb-2';
                    // Agregar id_datos como data attribute
                    if (comp.id_datos) div.dataset.idDatos = comp.id_datos;
                    div.innerHTML = `
                        <input type="text" placeholder="Componente" class="form-control me-2" 
                               style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;"
                               value="${comp.descripcion || ''}">
                        <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    componentesContainer.appendChild(div);
                });
                
                // Agregar un campo vacío para nuevo componente
                const div = document.createElement('div');
                div.className = 'd-flex mb-2';
                div.innerHTML = `
                    <input type="text" placeholder="Componente" class="form-control me-2" 
                           style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                componentesContainer.appendChild(div);
            }
        }
    }
    
    // Configurar eventos de los botones
    configurarEventosFormulario();
    
    // Mostrar el modal usando Bootstrap 5
    const modalElement = document.getElementById('modalProducto');
    if (modalElement) {
        // Actualizar el título del modal
        const tituloModal = document.getElementById('modalProductoLabel');
        if (tituloModal) {
            tituloModal.textContent = producto ? 'Editar Producto' : 'Agregar Producto';
        }
        
        // Mostrar el modal usando Bootstrap 5
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
}

// Configurar eventos de accesibilidad para el modal
const modalElement = document.getElementById('modalProducto');
if (modalElement) {
    // Manejar el evento cuando el modal se muestra
    modalElement.addEventListener('shown.bs.modal', function () {
        // Enfocar el primer elemento interactivo del modal
        const firstInput = modalElement.querySelector('input:not([type="hidden"]), button, [tabindex]:not([tabindex="-1"])');
        if (firstInput) {
            firstInput.focus();
        }
    });
    
    // Manejar el evento cuando el modal se oculta
    modalElement.addEventListener('hidden.bs.modal', function () {
        // Devolver el foco al botón que abrió el modal
        const btnAgregarProducto = document.getElementById('btnAgregarProducto');
        if (btnAgregarProducto) {
            btnAgregarProducto.focus();
        }
    });
}

// Eliminado el manejador de eventos de delegación duplicado

// Función para configurar los eventos del formulario
function configurarEventosFormulario() {
    // Función para manejar el scroll automático
    function manejarScroll(contenedor) {
        if (contenedor.children.length > 3) {
            // Hacer scroll hasta el final cuando se agrega un nuevo elemento
            contenedor.scrollTop = contenedor.scrollHeight;
        }
    }

    // Evento para agregar especificación
    const btnAgregarEspecificacion = document.getElementById('btnAgregarEspecificacion');
    if (btnAgregarEspecificacion) {
        btnAgregarEspecificacion.onclick = function() {
            const contenedor = document.getElementById('especificaciones');
            if (contenedor) {
                const nuevaEspecificacion = document.createElement('div');
                nuevaEspecificacion.className = 'd-flex mb-2';
                nuevaEspecificacion.innerHTML = `
                    <input type="text" placeholder="Característica" class="form-control me-2">
                    <input type="text" placeholder="Valor" class="form-control me-2">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                contenedor.appendChild(nuevaEspecificacion);
                
                // Manejar scroll
                manejarScroll(contenedor);
                
                // Agregar evento al botón de eliminar
                const btnEliminar = nuevaEspecificacion.querySelector('.btn-eliminar');
                if (btnEliminar) {
                    btnEliminar.onclick = function() {
                        contenedor.removeChild(nuevaEspecificacion);
                    };
                }
            }
        };
    }
    
    // Evento para agregar componente
    const btnAgregarComponente = document.getElementById('btnAgregarComponente');
    if (btnAgregarComponente) {
        btnAgregarComponente.onclick = function() {
            const contenedor = document.getElementById('componentes');
            if (contenedor) {
                const nuevoComponente = document.createElement('div');
                nuevoComponente.className = 'd-flex mb-2';
                nuevoComponente.innerHTML = `
                    <input type="text" placeholder="Componente" class="form-control me-2">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                contenedor.appendChild(nuevoComponente);
                
                // Manejar scroll
                manejarScroll(contenedor);
                
                // Agregar evento al botón de eliminar
                const btnEliminar = nuevoComponente.querySelector('.btn-eliminar');
                if (btnEliminar) {
                    btnEliminar.onclick = function() {
                        contenedor.removeChild(nuevoComponente);
                    };
                }
            }
        };
    }
    
    // Agregar eventos a los botones de eliminar existentes
    document.querySelectorAll('.btn-eliminar').forEach(btn => {
        btn.onclick = function() {
            const item = this.closest('.d-flex');
            if (item && item.parentNode) {
                item.parentNode.removeChild(item);
            }
        };
    });
}
