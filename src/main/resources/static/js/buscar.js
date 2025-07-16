document.addEventListener('DOMContentLoaded', function() {
    const inputBusqueda = document.querySelector('.input-busqueda');
    const botonBuscar = document.querySelector('.boton-buscar');
    const contenedorTiendas = document.querySelector('.contenedor-tiendas');
    const modalTienda = document.getElementById('modalTienda');
    const acordeonAlmacenes = document.getElementById('acordeonAlmacenes');
    let tiendasData = [];

    // Renderizar tarjetas de tiendas
    function renderizarTiendas(tiendas) {
        contenedorTiendas.innerHTML = '';
        if (!tiendas || tiendas.length === 0) {
            contenedorTiendas.innerHTML = '<div class="col-12 text-center text-muted">No se encontraron tiendas.</div>';
            return;
        }
        tiendas.forEach(tienda => {
            const col = document.createElement('div');
            col.className = 'col-12 col-sm-6 col-md-4 col-lg-3';
            col.innerHTML = `
                <div class="card tarjeta-tienda h-100 shadow-sm" data-id="${tienda.idTienda}">
                  <img src="${tienda.imagen ? 'data:image/jpeg;base64,' + tienda.imagen : 'https://via.placeholder.com/300x200?text=Sin+Imagen'}" class="card-img-top" alt="${tienda.nombreTienda}">
                  <div class="card-body d-flex flex-column justify-content-end">
                    <p class="card-text text-center fw-bold">${tienda.nombreTienda}</p>
                  </div>
                </div>
            `;
            contenedorTiendas.appendChild(col);
        });
    }

    // Buscar tiendas por modelo/serie
    async function buscarTiendas(query) {
        contenedorTiendas.innerHTML = '<div class="col-12 text-center text-muted">Buscando...</div>';
        let url = `/api/tiendas/buscar?query=${encodeURIComponent(query || '')}`;
        const resp = await fetch(url);
        tiendasData = await resp.json();
        renderizarTiendas(tiendasData);
    }

    // Mostrar modal con acordeón de almacenes y productos
    function mostrarModalTienda(tiendaId) {
        const tienda = tiendasData.find(t => t.idTienda == tiendaId);
        if (!tienda) return;
        document.getElementById('modalTiendaLabel').textContent = tienda.nombreTienda;
        const almacenes = tienda.almacenes || [];
        if (almacenes.length === 0) {
            acordeonAlmacenes.innerHTML = '<div class="text-muted">No hay almacenes con productos coincidentes.</div>';
        } else {
            acordeonAlmacenes.innerHTML = almacenes.map((almacen, idx) => `
                <div class="accordion-item">
                  <h2 class="accordion-header" id="heading${idx}">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#flush-collapse${idx}" aria-expanded="false" aria-controls="flush-collapse${idx}">
                      ${almacen.direccion}
                    </button>
                  </h2>
                  <div id="flush-collapse${idx}" class="accordion-collapse collapse" data-bs-parent="#acordeonAlmacenes">
                    <div class="accordion-body">
                      ${almacen.productos.map(producto => `
                        <div class="card mb-3">
                          <div class="row g-0">
                            <div class="col-md-4">
                              <img src="${producto.imagen ? 'data:image/jpeg;base64,' + producto.imagen : 'https://via.placeholder.com/200x200/000000/ffff00?text=Sin+Imagen'}" class="img-fluid rounded-start" alt="${producto.modelo}">
                            </div>
                            <div class="col-md-8">
                              <div class="card-body">
                                <h5 class="card-title">${producto.modelo}</h5>
                                <p class="card-text mb-1"><strong>N° Serie:</strong> ${producto.numeroSerie}</p>
                                <p class="card-text mb-1"><strong>Marca Ensambladora:</strong> ${producto.marcaEnsambladora}</p>
                                <p class="card-text mb-1"><strong>Marca Tarjeta:</strong> ${producto.marcaTarjeta}</p>
                                <p class="card-text mb-1"><strong>Estado:</strong> ${producto.estado}</p>
                                <p class="card-text"><strong>Descripción:</strong> ${producto.descripcion || 'Sin descripción'}</p>
                                <div class="accordion mt-3" id="acordeonProducto${producto.id}">
                                  <div class="accordion-item">
                                    <h2 class="accordion-header" id="headingEspec${producto.id}">
                                      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseEspec${producto.id}" aria-expanded="false" aria-controls="collapseEspec${producto.id}">
                                        Especificaciones técnicas
                                      </button>
                                    </h2>
                                    <div id="collapseEspec${producto.id}" class="accordion-collapse collapse" aria-labelledby="headingEspec${producto.id}" data-bs-parent="#acordeonProducto${producto.id}">
                                      <div class="accordion-body">
                                        <ul class="mb-0">
                                          ${producto.especificaciones && producto.especificaciones.length > 0 ? producto.especificaciones.map(e => `<li>${e.descripcion}${e.valor ? ': ' + e.valor : ''}</li>`).join('') : '<li>No especificadas</li>'}
                                        </ul>
                                      </div>
                                    </div>
                                  </div>
                                  <div class="accordion-item">
                                    <h2 class="accordion-header" id="headingComp${producto.id}">
                                      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseComp${producto.id}" aria-expanded="false" aria-controls="collapseComp${producto.id}">
                                        Dispositivos compatibles
                                      </button>
                                    </h2>
                                    <div id="collapseComp${producto.id}" class="accordion-collapse collapse" aria-labelledby="headingComp${producto.id}" data-bs-parent="#acordeonProducto${producto.id}">
                                      <div class="accordion-body">
                                        <ul class="mb-0">
                                          ${producto.compatibles && producto.compatibles.length > 0 ? producto.compatibles.map(c => `<li>${c.descripcion}</li>`).join('') : '<li>No especificados</li>'}
                                        </ul>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      `).join('')}
                    </div>
                  </div>
                </div>
            `).join('');
        }
        const modal = new bootstrap.Modal(modalTienda);
        modal.show();
    }

    // Eventos de búsqueda
    inputBusqueda.addEventListener('input', (e) => {
        buscarTiendas(e.target.value);
    });
    botonBuscar.addEventListener('click', () => {
        buscarTiendas(inputBusqueda.value);
    });
    inputBusqueda.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            buscarTiendas(inputBusqueda.value);
        }
    });

    // Evento para abrir modal al hacer clic en una tarjeta de tienda
    contenedorTiendas.addEventListener('click', (e) => {
        const card = e.target.closest('.tarjeta-tienda');
        if (card) {
            mostrarModalTienda(card.getAttribute('data-id'));
        }
    });

    // Cargar todas las tiendas al inicio
    buscarTiendas('');
});
