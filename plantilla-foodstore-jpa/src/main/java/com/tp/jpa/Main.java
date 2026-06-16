package com.tp.jpa;

import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.*;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Clase principal: menú de consola del sistema Food Store.
 * Orden de uso natural: Categorías -> Productos -> Usuarios -> Pedidos.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final CategoriaRepository categoriaRepo = new CategoriaRepository();
    private static final ProductoRepository productoRepo = new ProductoRepository();
    private static final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private static final PedidoRepository pedidoRepo = new PedidoRepository();

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("===== FOOD STORE - MENÚ PRINCIPAL =====");
            System.out.println("1. Gestionar Categorías");
            System.out.println("2. Gestionar Productos");
            System.out.println("3. Gestionar Usuarios");
            System.out.println("4. Gestionar Pedidos");
            System.out.println("5. Reportes");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1": menuCategorias(); break;
                case "2": menuProductos(); break;
                case "3": menuUsuarios(); break;
                case "4": menuPedidos(); break;
                case "5": menuReportes(); break;
                case "0": salir = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
        JPAUtil.close();
        System.out.println("Aplicación finalizada.");
    }

    // ── Submenús ─────────────────────────────────────────────────

    private static void menuCategorias() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("----- GESTIÓN DE CATEGORÍAS -----");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja lógica");
            System.out.println("4. Listado");
            System.out.println("0. Volver");
            switch (leer("Opción: ")) {
                case "1": altaCategoria(); break;
                case "2": modificarCategoria(); break;
                case "3": bajaCategoria(); break;
                case "4": listarCategorias(); break;
                case "0": volver = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void altaCategoria() {
        String nombre = leer("Nombre: ");
        if (nombre.isEmpty()) {
            System.out.println("Error: el nombre es obligatorio.");
            return;
        }
        String descripcion = leer("Descripción (opcional): ");
        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(descripcion.isEmpty() ? null : descripcion)
                .build();
        try {
            Categoria guardada = categoriaRepo.guardar(categoria);
            System.out.println("Categoría creada con ID " + guardada.getId() + ".");
        } catch (RuntimeException e) {
            System.out.println("Error al guardar la categoría (¿nombre repetido?).");
        }
    }

    private static void modificarCategoria() {
        List<Categoria> activas = categoriaRepo.listarActivos();
        if (activas.isEmpty()) {
            System.out.println("No hay categorías activas.");
            return;
        }
        mostrarCategorias(activas);
        Long id = leerId("ID de la categoría a modificar: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Categoria> opt = categoriaRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe una categoría activa con ese ID.");
            return;
        }
        Categoria categoria = opt.get();
        System.out.println("Nombre actual: " + categoria.getNombre());
        System.out.println("Descripción actual: "
                + (categoria.getDescripcion() == null ? "" : categoria.getDescripcion()));
        String nombre = leer("Nuevo nombre (Enter para conservar): ");
        if (!nombre.isEmpty()) {
            categoria.setNombre(nombre);
        }
        String descripcion = leer("Nueva descripción (Enter para conservar): ");
        if (!descripcion.isEmpty()) {
            categoria.setDescripcion(descripcion);
        }
        try {
            categoriaRepo.guardar(categoria);
            System.out.println("Categoría actualizada.");
        } catch (RuntimeException e) {
            System.out.println("Error al actualizar la categoría (¿nombre repetido?).");
        }
    }

    private static void bajaCategoria() {
        Long id = leerId("ID de la categoría a dar de baja: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Categoria> opt = categoriaRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe una categoría activa con ese ID.");
            return;
        }
        String nombre = opt.get().getNombre();
        if (categoriaRepo.eliminarLogico(id)) {
            System.out.println("Categoría \"" + nombre + "\" dada de baja.");
        } else {
            System.out.println("Error: no se pudo dar de baja la categoría.");
        }
    }

    private static void listarCategorias() {
        List<Categoria> activas = categoriaRepo.listarActivos();
        if (activas.isEmpty()) {
            System.out.println("No hay categorías activas.");
            return;
        }
        mostrarCategorias(activas);
    }

    private static void mostrarCategorias(List<Categoria> categorias) {
        System.out.println("ID | Nombre | Descripción");
        for (Categoria c : categorias) {
            System.out.println(c.getId() + " | " + c.getNombre() + " | "
                    + (c.getDescripcion() == null ? "" : c.getDescripcion()));
        }
    }

    private static void menuProductos() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("----- GESTIÓN DE PRODUCTOS -----");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja lógica");
            System.out.println("4. Listado");
            System.out.println("0. Volver");
            switch (leer("Opción: ")) {
                case "1": altaProducto(); break;
                case "2": modificarProducto(); break;
                case "3": bajaProducto(); break;
                case "4": listarProductos(); break;
                case "0": volver = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void altaProducto() {
        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) {
            System.out.println("Error: no hay categorías activas. Cree una categoría primero.");
            return;
        }
        mostrarCategorias(categorias);
        Long catId = leerId("ID de la categoría: ");
        if (catId == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Categoria> optCat = categoriaRepo.buscarPorId(catId);
        if (optCat.isEmpty() || optCat.get().isEliminado()) {
            System.out.println("Error: no existe una categoría activa con ese ID.");
            return;
        }
        String nombre = leer("Nombre: ");
        if (nombre.isEmpty()) {
            System.out.println("Error: el nombre es obligatorio.");
            return;
        }
        String descripcion = leer("Descripción (opcional): ");
        Double precio = leerDecimal("Precio: ");
        if (precio == null || precio <= 0) {
            System.out.println("Error: el precio debe ser un número mayor a 0.");
            return;
        }
        Integer stock = leerEntero("Stock: ");
        if (stock == null || stock < 0) {
            System.out.println("Error: el stock debe ser un número mayor o igual a 0.");
            return;
        }
        String imagen = leer("Imagen (opcional): ");
        String dispIn = leer("¿Disponible? (S/N, Enter = S): ");
        boolean disponible = dispIn.isEmpty() || dispIn.equalsIgnoreCase("S");
        Producto producto = Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion.isEmpty() ? null : descripcion)
                .precio(precio)
                .stock(stock)
                .imagen(imagen.isEmpty() ? null : imagen)
                .disponible(disponible)
                .build();
        try {
            Producto guardado = productoRepo.guardarEnCategoria(producto, catId);
            if (guardado == null) {
                System.out.println("Error: la categoría seleccionada no está disponible.");
                return;
            }
            System.out.println("Producto creado con ID " + guardado.getId()
                    + ", categoría: " + optCat.get().getNombre() + ".");
        } catch (RuntimeException e) {
            System.out.println("Error al guardar el producto.");
        }
    }

    private static void modificarProducto() {
        List<Producto> activos = productoRepo.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("No hay productos activos.");
            return;
        }
        mostrarProductos(activos);
        Long id = leerId("ID del producto a modificar: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Producto> opt = productoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un producto activo con ese ID.");
            return;
        }
        Producto producto = opt.get();
        System.out.println("Nombre actual: " + producto.getNombre());
        System.out.println("Precio actual: " + producto.getPrecio());
        System.out.println("Stock actual: " + producto.getStock());
        String nombre = leer("Nuevo nombre (Enter para conservar): ");
        if (!nombre.isEmpty()) {
            producto.setNombre(nombre);
        }
        String precioIn = leer("Nuevo precio (Enter para conservar): ");
        if (!precioIn.isEmpty()) {
            try {
                double precio = Double.parseDouble(precioIn.replace(",", "."));
                if (precio <= 0) {
                    System.out.println("Error: el precio debe ser mayor a 0. No se modificó.");
                    return;
                }
                producto.setPrecio(precio);
            } catch (NumberFormatException e) {
                System.out.println("Error: precio inválido. No se modificó.");
                return;
            }
        }
        String stockIn = leer("Nuevo stock (Enter para conservar): ");
        if (!stockIn.isEmpty()) {
            try {
                int stock = Integer.parseInt(stockIn);
                if (stock < 0) {
                    System.out.println("Error: el stock no puede ser negativo. No se modificó.");
                    return;
                }
                producto.setStock(stock);
            } catch (NumberFormatException e) {
                System.out.println("Error: stock inválido. No se modificó.");
                return;
            }
        }
        productoRepo.guardar(producto);
        System.out.println("Producto actualizado.");
    }

    private static void bajaProducto() {
        Long id = leerId("ID del producto a dar de baja: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Producto> opt = productoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un producto activo con ese ID.");
            return;
        }
        String nombre = opt.get().getNombre();
        if (productoRepo.eliminarLogico(id)) {
            System.out.println("Producto \"" + nombre + "\" dado de baja.");
        } else {
            System.out.println("Error: no se pudo dar de baja el producto.");
        }
    }

    private static void listarProductos() {
        List<Producto> activos = productoRepo.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("No hay productos activos.");
            return;
        }
        mostrarProductos(activos);
    }

    private static void mostrarProductos(List<Producto> productos) {
        System.out.println("ID | Nombre | Precio | Stock | Disponible | Categoría");
        for (Producto p : productos) {
            String categoria = productoRepo.buscarCategoriaPorProducto(p.getId())
                    .map(Categoria::getNombre)
                    .orElse("(sin categoría)");
            System.out.println(p.getId() + " | " + p.getNombre() + " | " + p.getPrecio()
                    + " | " + p.getStock() + " | "
                    + (Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No")
                    + " | " + categoria);
        }
    }

    private static void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("----- GESTIÓN DE USUARIOS -----");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja lógica");
            System.out.println("4. Listado");
            System.out.println("5. Buscar por mail");
            System.out.println("0. Volver");
            switch (leer("Opción: ")) {
                case "1": altaUsuario(); break;
                case "2": modificarUsuario(); break;
                case "3": bajaUsuario(); break;
                case "4": listarUsuarios(); break;
                case "5": buscarUsuarioPorMail(); break;
                case "0": volver = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void altaUsuario() {
        String nombre = leer("Nombre: ");
        if (nombre.isEmpty()) {
            System.out.println("Error: el nombre es obligatorio.");
            return;
        }
        String apellido = leer("Apellido: ");
        if (apellido.isEmpty()) {
            System.out.println("Error: el apellido es obligatorio.");
            return;
        }
        String mail = leer("Mail: ");
        if (mail.isEmpty()) {
            System.out.println("Error: el mail es obligatorio.");
            return;
        }
        if (usuarioRepo.buscarPorMail(mail).isPresent()) {
            System.out.println("Error: ya existe un usuario activo con ese mail.");
            return;
        }
        String celular = leer("Celular (opcional): ");
        String contrasena = leer("Contraseña: ");
        if (contrasena.isEmpty()) {
            System.out.println("Error: la contraseña es obligatoria.");
            return;
        }
        Rol rol = leerRol();
        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .mail(mail)
                .celular(celular.isEmpty() ? null : celular)
                .contraseña(contrasena)
                .rol(rol)
                .build();
        try {
            Usuario guardado = usuarioRepo.guardar(usuario);
            System.out.println("Usuario creado con ID " + guardado.getId() + ".");
        } catch (RuntimeException e) {
            System.out.println("Error al guardar el usuario (¿mail repetido?).");
        }
    }

    private static void modificarUsuario() {
        List<Usuario> activos = usuarioRepo.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("No hay usuarios activos.");
            return;
        }
        mostrarUsuarios(activos);
        Long id = leerId("ID del usuario a modificar: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Usuario> opt = usuarioRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un usuario activo con ese ID.");
            return;
        }
        Usuario usuario = opt.get();
        System.out.println("Nombre actual: " + usuario.getNombre());
        System.out.println("Apellido actual: " + usuario.getApellido());
        System.out.println("Mail actual: " + usuario.getMail());
        System.out.println("Celular actual: "
                + (usuario.getCelular() == null ? "" : usuario.getCelular()));
        String nombre = leer("Nuevo nombre (Enter para conservar): ");
        if (!nombre.isEmpty()) {
            usuario.setNombre(nombre);
        }
        String apellido = leer("Nuevo apellido (Enter para conservar): ");
        if (!apellido.isEmpty()) {
            usuario.setApellido(apellido);
        }
        String mailIn = leer("Nuevo mail (Enter para conservar): ");
        if (!mailIn.isEmpty() && !mailIn.equals(usuario.getMail())) {
            Optional<Usuario> existente = usuarioRepo.buscarPorMail(mailIn);
            if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
                System.out.println("Error: el mail ya está en uso por otro usuario. No se modificó.");
                return;
            }
            usuario.setMail(mailIn);
        }
        String celular = leer("Nuevo celular (Enter para conservar): ");
        if (!celular.isEmpty()) {
            usuario.setCelular(celular);
        }
        String contrasena = leer("Nueva contraseña (Enter para conservar): ");
        if (!contrasena.isEmpty()) {
            usuario.setContraseña(contrasena);
        }
        try {
            usuarioRepo.guardar(usuario);
            System.out.println("Usuario actualizado.");
        } catch (RuntimeException e) {
            System.out.println("Error al actualizar el usuario.");
        }
    }

    private static void bajaUsuario() {
        Long id = leerId("ID del usuario a dar de baja: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Usuario> opt = usuarioRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un usuario activo con ese ID.");
            return;
        }
        String nombreCompleto = opt.get().getNombre() + " " + opt.get().getApellido();
        if (usuarioRepo.eliminarLogico(id)) {
            System.out.println("Usuario \"" + nombreCompleto + "\" dado de baja. Sus pedidos se conservan.");
        } else {
            System.out.println("Error: no se pudo dar de baja el usuario.");
        }
    }

    private static void listarUsuarios() {
        List<Usuario> activos = usuarioRepo.listarActivos();
        if (activos.isEmpty()) {
            System.out.println("No hay usuarios activos.");
            return;
        }
        mostrarUsuarios(activos);
    }

    private static void buscarUsuarioPorMail() {
        String mail = leer("Mail a buscar: ");
        if (mail.isEmpty()) {
            System.out.println("Error: el mail es obligatorio.");
            return;
        }
        Optional<Usuario> opt = usuarioRepo.buscarPorMail(mail);
        if (opt.isEmpty()) {
            System.out.println("No existe un usuario activo con ese mail.");
            return;
        }
        Usuario u = opt.get();
        System.out.println("ID: " + u.getId());
        System.out.println("Nombre: " + u.getNombre() + " " + u.getApellido());
        System.out.println("Mail: " + u.getMail());
        System.out.println("Celular: " + (u.getCelular() == null ? "" : u.getCelular()));
        System.out.println("Rol: " + u.getRol());
    }

    private static void mostrarUsuarios(List<Usuario> usuarios) {
        System.out.println("ID | Nombre completo | Mail | Rol");
        for (Usuario u : usuarios) {
            System.out.println(u.getId() + " | " + u.getNombre() + " " + u.getApellido()
                    + " | " + u.getMail() + " | " + u.getRol());
        }
    }

    private static void menuPedidos() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("----- GESTIÓN DE PEDIDOS -----");
            System.out.println("1. Alta de pedido");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Baja lógica");
            System.out.println("4. Listado");
            System.out.println("5. Pedidos por usuario");
            System.out.println("6. Pedidos por estado");
            System.out.println("0. Volver");
            switch (leer("Opción: ")) {
                case "1": altaPedido(); break;
                case "2": cambiarEstadoPedido(); break;
                case "3": bajaPedido(); break;
                case "4": listarPedidos(); break;
                case "5": pedidosPorUsuario(); break;
                case "6": pedidosPorEstado(); break;
                case "0": volver = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void altaPedido() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) {
            System.out.println("Error: no hay usuarios activos. Cree un usuario primero.");
            return;
        }
        mostrarUsuarios(usuarios);
        Long usuarioId = leerId("ID del usuario: ");
        if (usuarioId == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Usuario> optUsuario = usuarioRepo.buscarPorId(usuarioId);
        if (optUsuario.isEmpty() || optUsuario.get().isEliminado()) {
            System.out.println("Error: no existe un usuario activo con ese ID.");
            return;
        }
        FormaPago formaPago = leerFormaPago();

        // Lista temporal en memoria: idProducto -> cantidad (aún no se persiste nada).
        Map<Long, Integer> items = new LinkedHashMap<>();
        boolean seguir = true;
        while (seguir) {
            List<Producto> productos = productoRepo.listarActivos();
            if (productos.isEmpty()) {
                System.out.println("No hay productos activos para agregar.");
                break;
            }
            System.out.println("ID | Nombre | Precio | Stock | Disponible");
            for (Producto p : productos) {
                System.out.println(p.getId() + " | " + p.getNombre() + " | " + p.getPrecio()
                        + " | " + p.getStock() + " | "
                        + (Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No"));
            }
            Long prodId = leerId("ID del producto a agregar: ");
            if (prodId == null) {
                System.out.println("Error: ID inválido.");
            } else {
                Optional<Producto> optProd = productoRepo.buscarPorId(prodId);
                if (optProd.isEmpty() || optProd.get().isEliminado()) {
                    System.out.println("Error: no existe un producto activo con ese ID.");
                } else if (!Boolean.TRUE.equals(optProd.get().getDisponible())) {
                    System.out.println("Error: el producto no está disponible.");
                } else {
                    Integer cantidad = leerEntero("Cantidad: ");
                    if (cantidad == null || cantidad <= 0) {
                        System.out.println("Error: la cantidad debe ser un entero mayor a 0.");
                    } else {
                        int yaPedido = items.getOrDefault(prodId, 0);
                        if (optProd.get().getStock() < yaPedido + cantidad) {
                            System.out.println("Error: stock insuficiente. Disponible: "
                                    + (optProd.get().getStock() - yaPedido));
                        } else {
                            items.merge(prodId, cantidad, Integer::sum);
                            System.out.println("Agregado: " + optProd.get().getNombre()
                                    + " x" + cantidad + ".");
                        }
                    }
                }
            }
            seguir = leer("¿Agregar otro producto? (S/N): ").equalsIgnoreCase("S");
        }

        if (items.isEmpty()) {
            System.out.println("El pedido debe tener al menos un producto. Operación cancelada.");
            return;
        }

        try {
            Pedido pedido = pedidoRepo.crearPedido(usuarioId, formaPago, items);
            if (pedido == null) {
                System.out.println("Error: el usuario seleccionado no está disponible.");
                return;
            }
            System.out.println("Pedido creado con ID " + pedido.getId() + ".");
            System.out.println("Fecha: " + pedido.getFecha());
            System.out.println("Usuario: " + optUsuario.get().getNombre()
                    + " " + optUsuario.get().getApellido());
            System.out.println("Forma de pago: " + pedido.getFormaPago());
            System.out.println("Detalle:");
            for (DetallePedido d : pedido.getDetalles()) {
                System.out.println("  - " + d.getProducto().getNombre() + " x" + d.getCantidad()
                        + " = " + String.format(Locale.US, "$%.2f", d.getSubtotal()));
            }
            System.out.println("Total: " + String.format(Locale.US, "$%.2f", pedido.getTotal()));
        } catch (RuntimeException e) {
            System.out.println("Error al crear el pedido: " + e.getMessage());
        }
    }

    private static void cambiarEstadoPedido() {
        Long id = leerId("ID del pedido: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un pedido activo con ese ID.");
            return;
        }
        Pedido pedido = opt.get();
        System.out.println("Estado actual: " + pedido.getEstado());
        Estado nuevo = leerEstado();
        pedido.setEstado(nuevo);
        pedidoRepo.guardar(pedido);
        System.out.println("Pedido " + pedido.getId() + " actualizado al estado " + nuevo + ".");
    }

    private static void bajaPedido() {
        Long id = leerId("ID del pedido a dar de baja: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe un pedido activo con ese ID.");
            return;
        }
        Double total = opt.get().getTotal();
        if (pedidoRepo.eliminarLogico(id)) {
            System.out.println("Pedido " + id + " dado de baja. Total: "
                    + String.format(Locale.US, "$%.2f", total == null ? 0.0 : total)
                    + ". El stock no se restaura.");
        } else {
            System.out.println("Error: no se pudo dar de baja el pedido.");
        }
    }

    private static void listarPedidos() {
        List<Pedido> pedidos = pedidoRepo.listarActivos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos activos.");
            return;
        }
        System.out.println("ID | Fecha | Estado | Forma de pago | Usuario | Total");
        for (Pedido p : pedidos) {
            String usuario = pedidoRepo.buscarUsuarioPorPedido(p.getId())
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("(sin usuario)");
            System.out.println(p.getId() + " | " + p.getFecha() + " | " + p.getEstado()
                    + " | " + p.getFormaPago() + " | " + usuario + " | "
                    + String.format(Locale.US, "$%.2f", p.getTotal() == null ? 0.0 : p.getTotal()));
        }
    }

    private static void pedidosPorUsuario() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios activos.");
            return;
        }
        mostrarUsuarios(usuarios);
        Long id = leerId("ID del usuario: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        List<Pedido> pedidos = pedidoRepo.buscarPorUsuario(id);
        if (pedidos.isEmpty()) {
            System.out.println("El usuario no tiene pedidos activos.");
            return;
        }
        System.out.println("ID | Fecha | Estado | Forma de pago | Total");
        for (Pedido p : pedidos) {
            System.out.println(p.getId() + " | " + p.getFecha() + " | " + p.getEstado()
                    + " | " + p.getFormaPago() + " | "
                    + String.format(Locale.US, "$%.2f", p.getTotal() == null ? 0.0 : p.getTotal()));
        }
    }

    private static void pedidosPorEstado() {
        Estado estado = leerEstado();
        List<Pedido> pedidos = pedidoRepo.buscarPorEstado(estado);
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos con estado " + estado + ".");
            return;
        }
        System.out.println("ID | Fecha | Usuario | Total");
        for (Pedido p : pedidos) {
            String usuario = pedidoRepo.buscarUsuarioPorPedido(p.getId())
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("(sin usuario)");
            System.out.println(p.getId() + " | " + p.getFecha() + " | " + usuario + " | "
                    + String.format(Locale.US, "$%.2f", p.getTotal() == null ? 0.0 : p.getTotal()));
        }
    }

    private static void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("----- REPORTES -----");
            System.out.println("1. Productos por categoría");
            System.out.println("2. Pedidos por usuario");
            System.out.println("3. Pedidos por estado");
            System.out.println("4. Total facturado");
            System.out.println("0. Volver");
            switch (leer("Opción: ")) {
                case "1": reporteProductosPorCategoria(); break;
                case "2": pedidosPorUsuario(); break;   // reutiliza la consulta del menú de Pedidos
                case "3": pedidosPorEstado(); break;     // reutiliza la consulta del menú de Pedidos
                case "4": reporteTotalFacturado(); break;
                case "0": volver = true; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void reporteProductosPorCategoria() {
        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) {
            System.out.println("No hay categorías activas.");
            return;
        }
        mostrarCategorias(categorias);
        Long id = leerId("ID de la categoría: ");
        if (id == null) {
            System.out.println("Error: ID inválido.");
            return;
        }
        List<Producto> productos = productoRepo.buscarPorCategoria(id);
        if (productos.isEmpty()) {
            System.out.println("La categoría no tiene productos activos.");
            return;
        }
        System.out.println("ID | Nombre | Precio | Stock");
        for (Producto p : productos) {
            System.out.println(p.getId() + " | " + p.getNombre() + " | " + p.getPrecio()
                    + " | " + p.getStock());
        }
    }

    private static void reporteTotalFacturado() {
        List<Pedido> terminados = pedidoRepo.buscarPorEstado(Estado.TERMINADO);
        double total = terminados.stream()
                .mapToDouble(p -> p.getTotal() == null ? 0.0 : p.getTotal())
                .sum();
        System.out.println("Total facturado: " + String.format(Locale.US, "$%.2f", total));
    }

    // ── Helpers de entrada ───────────────────────────────────────

    /** Muestra el prompt y devuelve la línea ingresada sin espacios extra. */
    private static String leer(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /** Lee un ID (Long). Devuelve null si lo ingresado no es un número válido. */
    private static Long leerId(String prompt) {
        String entrada = leer(prompt);
        try {
            return Long.parseLong(entrada);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Lee un decimal. Devuelve null si está vacío o no es un número válido. */
    private static Double leerDecimal(String prompt) {
        String entrada = leer(prompt);
        if (entrada.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(entrada.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Lee un entero. Devuelve null si está vacío o no es un número válido. */
    private static Integer leerEntero(String prompt) {
        String entrada = leer(prompt);
        if (entrada.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Solicita el rol hasta recibir una opción válida (1-ADMIN, 2-USUARIO). */
    private static Rol leerRol() {
        while (true) {
            String entrada = leer("Rol (1-ADMIN, 2-USUARIO): ");
            if (entrada.equals("1")) {
                return Rol.ADMIN;
            }
            if (entrada.equals("2")) {
                return Rol.USUARIO;
            }
            System.out.println("Opción inválida, intente de nuevo.");
        }
    }

    /** Solicita la forma de pago hasta recibir una opción válida. */
    private static FormaPago leerFormaPago() {
        while (true) {
            switch (leer("Forma de pago (1-TARJETA, 2-TRANSFERENCIA, 3-EFECTIVO): ")) {
                case "1": return FormaPago.TARJETA;
                case "2": return FormaPago.TRANSFERENCIA;
                case "3": return FormaPago.EFECTIVO;
                default: System.out.println("Opción inválida, intente de nuevo.");
            }
        }
    }

    /** Solicita un estado de pedido hasta recibir una opción válida. */
    private static Estado leerEstado() {
        while (true) {
            switch (leer("Estado (1-PENDIENTE, 2-CONFIRMADO, 3-TERMINADO, 4-CANCELADO): ")) {
                case "1": return Estado.PENDIENTE;
                case "2": return Estado.CONFIRMADO;
                case "3": return Estado.TERMINADO;
                case "4": return Estado.CANCELADO;
                default: System.out.println("Opción inválida, intente de nuevo.");
            }
        }
    }

}
