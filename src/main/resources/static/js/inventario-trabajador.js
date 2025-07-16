// === inventario-trabajador.js ===
// Basado en inventario-nuevo.js, adaptado para el contexto de trabajador

console.log('=== Cargando inventario-trabajador.js ===');

// Configuración global para fetch
const fetchConfig = {
    credentials: 'include',
    headers: {
        'Content-Type': 'application/json'
    }
};

const almacenes = [];
const productos = [];
let almacenSeleccionado = null;

function mostrarError(mensaje) {
    console.error(mensaje);
    alert(mensaje);
}

window.seleccionarAlmacen = async function(id) {
    almacenSeleccionado = id;
    const almacen = almacenes.find(a => a.id_almacen === id);
    if (almacen) {
        try {
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
            // Cambiar la ruta para trabajador
            const response = await fetch(`/api/trabajador/almacenes/${id}/productos`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Error al cargar productos: ${response.status}`);
            }
            const data = await response.json();
            productos.length = 0;
            if (Array.isArray(data)) {
                productos.push(...data);
            }
            renderizarProductos();
            const tituloInventario = document.getElementById('tituloInventario');
            if (tituloInventario) {
                tituloInventario.textContent = `Inventario - ${almacen.nombre}`;
            }
            const btnAgregarProducto = document.getElementById('btnAgregarProducto');
            if (btnAgregarProducto) {
                btnAgregarProducto.disabled = false;
            }
        } catch (error) {
            mostrarError('No se pudieron cargar los productos del almacén: ' + error.message);
        }
    }
};



function limpiarModal() {
    const backdrop = document.querySelector('.modal-backdrop');
    if (backdrop) {
        backdrop.remove();
    }
    document.body.style.overflow = 'auto';
    document.body.style.paddingRight = '0';
}

document.addEventListener('DOMContentLoaded', function() {
    const btnAgregarProducto = document.getElementById('btnAgregarProducto');
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
    cargarAlmacenes();
    // ... (el resto de la lógica de formularios es igual que en inventario-nuevo.js)
});

async function cargarAlmacenes() {
    try {
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
        almacenes.length = 0;
        // Cambiar la ruta para trabajador
        const response = await fetch('/api/trabajador/almacenes', {
            credentials: 'include'
        });
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('No tiene permisos para ver los almacenes');
            } else {
                throw new Error(`Error HTTP: ${response.status}`);
            }
        }
        const data = await response.json();
        data.forEach(almacen => {
            almacenes.push({
                id_almacen: almacen.id_almacen,
                nombre: almacen.nombre,
                direccion: almacen.direccion,
                productos: almacen.productos || []
            });
        });
        renderizarAlmacenes();
    } catch (error) {
        mostrarError('No se pudieron cargar los almacenes: ' + error.message);
    }
}

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

        </div>
    `).join('');
    listaAlmacenes.innerHTML = html;
}

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
    productos.forEach(producto => {
        const datosProducto = {
            id: producto.id_producto,
            marcaEnsambladora: (producto.marcaEnsambladora || '').trim(),
            marcaTarjeta: (producto.marcaTarjeta || '').trim(),
            modelo: (producto.modelo || '').trim(),
            descripcion: (producto.descripcion || '').trim(),
            numeroSerie: (producto.numeroSerie || '').trim(),
            imagen: producto.imagenBase64 || '',
            estado: (producto.estado || 'Sin estado').trim()
        };
        if (!datosProducto.modelo) datosProducto.modelo = 'Sin modelo';
        if (!datosProducto.marcaTarjeta) datosProducto.marcaTarjeta = 'Sin marca';
        if (!datosProducto.marcaEnsambladora) datosProducto.marcaEnsambladora = 'Sin ensambladora';
        if (!datosProducto.descripcion) datosProducto.descripcion = 'Sin descripción';
        if (!datosProducto.numeroSerie) datosProducto.numeroSerie = 'N/A';
        html += `
            <div class="col-md-4 mb-4">
                <div class="card h-100 producto-card" style="background-color: #1a1a1a; border: 1px solid #ff0; color: white;">
                    ${datosProducto.imagen ? `
                        <img src="${datosProducto.imagen}" 
                             class="card-img-top" 
                             alt="${datosProducto.marcaTarjeta} ${datosProducto.modelo}" 
                             onerror="this.onerror=null; this.src='data:image/svg+xml;charset=UTF-8,%3Csvg width='200' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Crect width='100%' height='100%' fill='%23000'/%3E%3Ctext x='50%' y='50%' font-family='sans-serif' font-size='14' fill='%23ff0' text-anchor='middle' dominant-baseline='middle'%3EImagen no disponible%3C/text%3E%3C/svg%3E'"
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
                        <!-- Botón de eliminar eliminado para trabajadores -->
                    </div>
                </div>
            </div>`;
    });
    listaProductos.innerHTML = html;
    listaProductos.addEventListener('click', function(e) {
        const deleteBtn = e.target.closest('.btn-eliminar');
        const editBtn = e.target.closest('.btn-editar');
        if (deleteBtn) {
            e.preventDefault();
            e.stopPropagation();
            const id = deleteBtn.getAttribute('data-id');
            if (id) {
                // Eliminar toda la lógica y botones relacionados con la eliminación de productos
            }
            return false;
        }
        if (editBtn) {
            e.preventDefault();
            e.stopPropagation();
            const id = editBtn.getAttribute('data-id');
            if (id) {
                editarProducto(id, e);
            }
            return false;
        }
    });
}

// ...
// El resto de la lógica de formularios y modales es igual que en inventario-nuevo.js, pero usando rutas /api/trabajador/...
// Puedes copiar y adaptar según lo necesites.



// Función para mostrar el formulario de producto
function mostrarFormularioProducto(producto = null) {
    console.log('Mostrando formulario para producto:', producto);
    if (!almacenSeleccionado && !producto) {
        mostrarError('Por favor seleccione un almacén primero');
        return;
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
            const productoId = producto.id_producto || producto.id || '';
            document.getElementById('productoId').value = productoId;
            console.log('ID del producto establecido en el formulario:', productoId);
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
            especificacionesContainer.innerHTML = '';
            
            // Si es edición, cargar especificaciones existentes
            if (producto && producto.especificaciones && producto.especificaciones.length > 0) {
                producto.especificaciones.forEach(espec => {
                    const div = document.createElement('div');
                    div.className = 'd-flex mb-2';
                    // Agregar id_datos como data attribute
                    if (espec.id_datos) div.dataset.idDatos = espec.id_datos;
                    div.innerHTML = `
                        <input type="text" placeholder="Característica" class="form-control me-2" value="${espec.descripcion || ''}" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                        <input type="text" placeholder="Valor" class="form-control me-2" value="${espec.valor || ''}" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                        <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    especificacionesContainer.appendChild(div);
                });
            } else {
                // Agregar una fila vacía por defecto
                const div = document.createElement('div');
                div.className = 'd-flex mb-2';
                div.innerHTML = `
                    <input type="text" placeholder="Característica" class="form-control me-2" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <input type="text" placeholder="Valor" class="form-control me-2" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                    <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                especificacionesContainer.appendChild(div);
            }
        }
        
        if (componentesContainer) {
            componentesContainer.innerHTML = '';
            
            // Si es edición, cargar componentes existentes
            if (producto && producto.componentes && producto.componentes.length > 0) {
                producto.componentes.forEach(comp => {
                    const div = document.createElement('div');
                    div.className = 'd-flex mb-2';
                    // Agregar id_datos como data attribute
                    if (comp.id_datos) div.dataset.idDatos = comp.id_datos;
                    div.innerHTML = `
                        <input type="text" placeholder="Componente" class="form-control me-2" value="${comp.descripcion || ''}" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
                        <button type="button" class="btn btn-sm btn-danger btn-eliminar">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    componentesContainer.appendChild(div);
                });
            } else {
                // Agregar una fila vacía por defecto
                const div = document.createElement('div');
                div.className = 'd-flex mb-2';
                div.innerHTML = `
                    <input type="text" placeholder="Componente" class="form-control me-2" style="background: rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 0, 0.3); color: white;">
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

// Función para editar producto
async function editarProducto(id) {
    try {
        const response = await fetch(`/api/trabajador/productos/${id}`);
        if (!response.ok) {
            throw new Error('Error al cargar el producto');
        }
        
        const producto = await response.json();
        console.log('Producto cargado para edición:', producto);
        
        // Mostrar el formulario con los datos del producto
        mostrarFormularioProducto(producto);
        
    } catch (error) {
        console.error('Error al cargar el producto:', error);
        mostrarError('Error al cargar el producto: ' + error.message);
    }
}

// Eliminar toda la lógica y botones relacionados con la eliminación de productos

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

// Configurar eventos adicionales cuando se carga el DOM
document.addEventListener('DOMContentLoaded', function() {

    
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
            
            // Obtener el ID del producto del campo oculto
            const productoId = document.getElementById('productoId').value;
            console.log('ID del producto obtenido:', productoId);
            console.log('¿Es edición?', !!productoId);
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
                const url = productoId ? `/api/trabajador/productos/${productoId}` : '/api/trabajador/productos';
                const method = productoId ? 'PUT' : 'POST';
                console.log('URL de la petición:', url);
                console.log('Método de la petición:', method);
                
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
                
                // El ID del producto se maneja en la URL para PUT, no en FormData
                
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
                
                // Cerrar el modal
                const modalElement = document.getElementById('modalProducto');
                if (modalElement) {
                    const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
                    modal.hide();
                }
                
            } catch (error) {
                console.error('Error al guardar el producto:', error);
                mostrarError('No se pudo guardar el producto: ' + error.message);
            }
        });
    }
}); 