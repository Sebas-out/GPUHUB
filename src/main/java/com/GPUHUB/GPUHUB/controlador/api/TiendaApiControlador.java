package com.GPUHUB.GPUHUB.controlador.api;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.repositorio.TiendaRepositorio;
import com.GPUHUB.GPUHUB.repositorio.ProductoRepositorio;
import com.GPUHUB.GPUHUB.repositorio.AlmacenRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaApiControlador {
    @Autowired
    private TiendaRepositorio tiendaRepositorio;
    @Autowired
    private ProductoRepositorio productoRepositorio;
    @Autowired
    private AlmacenRepositorio almacenRepositorio;

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarTiendasPorProducto(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            // Si el query está vacío, devolver todas las tiendas con todos sus almacenes y productos
            List<Tienda> tiendas = tiendaRepositorio.findAll();
            List<Map<String, Object>> tiendasResponse = new ArrayList<>();
            for (Tienda tienda : tiendas) {
                Map<String, Object> tiendaData = new HashMap<>();
                tiendaData.put("idTienda", tienda.getIdTienda());
                tiendaData.put("nombreTienda", tienda.getNombreTienda());
                tiendaData.put("imagen", tienda.getImagen() != null ? Base64.getEncoder().encodeToString(tienda.getImagen()) : null);
                List<Almacen> almacenes = almacenRepositorio.findByTiendaIdTienda(tienda.getIdTienda());
                List<Map<String, Object>> almacenesResponse = new ArrayList<>();
                for (Almacen almacen : almacenes) {
                    Map<String, Object> almacenData = new HashMap<>();
                    almacenData.put("idAlmacen", almacen.getId_almacen());
                    almacenData.put("nombre", almacen.getNombre());
                    almacenData.put("direccion", almacen.getDireccion());
                    List<Producto> productos = almacen.getProductos();
                    List<Map<String, Object>> productosList = new ArrayList<>();
                    if (productos != null) {
                        for (Producto producto : productos) {
                            Map<String, Object> productoData = new HashMap<>();
                            productoData.put("id", producto.getId_producto());
                            productoData.put("modelo", producto.getModelo());
                            productoData.put("numeroSerie", producto.getNumeroSerie());
                            productoData.put("marcaEnsambladora", producto.getMarcaEnsambladora());
                            productoData.put("marcaTarjeta", producto.getMarcaTarjeta());
                            productoData.put("descripcion", producto.getDescripcion());
                            productoData.put("estado", producto.getEstado());
                            productoData.put("imagen", producto.getImagen() != null ? Base64.getEncoder().encodeToString(producto.getImagen()) : null);
                            // Especificaciones técnicas (tipo 1)
                            List<Map<String, String>> especificaciones = new ArrayList<>();
                            List<Map<String, String>> compatibles = new ArrayList<>();
                            if (producto.getDatos() != null) {
                                for (var dato : producto.getDatos()) {
                                    if (dato.getTipo() != null && dato.getTipo() == 1) {
                                        Map<String, String> espec = new HashMap<>();
                                        espec.put("descripcion", dato.getDescripcion());
                                        espec.put("valor", dato.getValor());
                                        especificaciones.add(espec);
                                    } else if (dato.getTipo() != null && dato.getTipo() == 0) {
                                        Map<String, String> comp = new HashMap<>();
                                        comp.put("descripcion", dato.getDescripcion());
                                        compatibles.add(comp);
                                    }
                                }
                            }
                            productoData.put("especificaciones", especificaciones);
                            productoData.put("compatibles", compatibles);
                            productosList.add(productoData);
                        }
                    }
                    almacenData.put("productos", productosList);
                    almacenesResponse.add(almacenData);
                }
                tiendaData.put("almacenes", almacenesResponse);
                tiendasResponse.add(tiendaData);
            }
            return ResponseEntity.ok(tiendasResponse);
        } else {
            // Buscar por cualquier criterio
            List<Producto> productos;
            productos = productoRepositorio.buscarPorCualquierCriterio(query);
            Map<Long, Map<String, Object>> tiendasMap = new HashMap<>();
            for (Producto producto : productos) {
                Almacen almacen = producto.getAlmacen();
                Tienda tienda = almacen.getTienda();
                if (!tiendasMap.containsKey(tienda.getIdTienda())) {
                    Map<String, Object> tiendaData = new HashMap<>();
                    tiendaData.put("idTienda", tienda.getIdTienda());
                    tiendaData.put("nombreTienda", tienda.getNombreTienda());
                    tiendaData.put("imagen", tienda.getImagen() != null ? Base64.getEncoder().encodeToString(tienda.getImagen()) : null);
                    tiendaData.put("almacenes", new HashMap<Long, Map<String, Object>>());
                    tiendasMap.put(tienda.getIdTienda(), tiendaData);
                }
                Map<Long, Map<String, Object>> almacenesMap = (Map<Long, Map<String, Object>>) tiendasMap.get(tienda.getIdTienda()).get("almacenes");
                if (!almacenesMap.containsKey(almacen.getId_almacen())) {
                    Map<String, Object> almacenData = new HashMap<>();
                    almacenData.put("idAlmacen", almacen.getId_almacen());
                    almacenData.put("nombre", almacen.getNombre());
                    almacenData.put("direccion", almacen.getDireccion());
                    almacenData.put("productos", new ArrayList<Map<String, Object>>());
                    almacenesMap.put(almacen.getId_almacen(), almacenData);
                }
                List<Map<String, Object>> productosList = (List<Map<String, Object>>) almacenesMap.get(almacen.getId_almacen()).get("productos");
                Map<String, Object> productoData = new HashMap<>();
                productoData.put("id", producto.getId_producto());
                productoData.put("modelo", producto.getModelo());
                productoData.put("numeroSerie", producto.getNumeroSerie());
                productoData.put("marcaEnsambladora", producto.getMarcaEnsambladora());
                productoData.put("marcaTarjeta", producto.getMarcaTarjeta());
                productoData.put("descripcion", producto.getDescripcion());
                productoData.put("estado", producto.getEstado());
                productoData.put("imagen", producto.getImagen() != null ? Base64.getEncoder().encodeToString(producto.getImagen()) : null);
                // Especificaciones técnicas (tipo 1)
                List<Map<String, String>> especificaciones = new ArrayList<>();
                List<Map<String, String>> compatibles = new ArrayList<>();
                if (producto.getDatos() != null) {
                    for (var dato : producto.getDatos()) {
                        if (dato.getTipo() != null && dato.getTipo() == 1) {
                            Map<String, String> espec = new HashMap<>();
                            espec.put("descripcion", dato.getDescripcion());
                            espec.put("valor", dato.getValor());
                            especificaciones.add(espec);
                        } else if (dato.getTipo() != null && dato.getTipo() == 0) {
                            Map<String, String> comp = new HashMap<>();
                            comp.put("descripcion", dato.getDescripcion());
                            compatibles.add(comp);
                        }
                    }
                }
                productoData.put("especificaciones", especificaciones);
                productoData.put("compatibles", compatibles);
                productosList.add(productoData);
            }
            // Convertir el map a lista para la respuesta
            List<Map<String, Object>> tiendas = new ArrayList<>();
            for (Map<String, Object> tiendaData : tiendasMap.values()) {
                Map<Long, Map<String, Object>> almacenesMap = (Map<Long, Map<String, Object>>) tiendaData.get("almacenes");
                tiendaData.put("almacenes", new ArrayList<>(almacenesMap.values()));
                tiendas.add(tiendaData);
            }
            return ResponseEntity.ok(tiendas);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodasLasTiendas() {
        List<Tienda> tiendas = tiendaRepositorio.findAll();
        List<Map<String, Object>> response = tiendas.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idTienda", t.getIdTienda());
            map.put("nombreTienda", t.getNombreTienda());
            map.put("imagen", t.getImagen() != null ? Base64.getEncoder().encodeToString(t.getImagen()) : null);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
} 