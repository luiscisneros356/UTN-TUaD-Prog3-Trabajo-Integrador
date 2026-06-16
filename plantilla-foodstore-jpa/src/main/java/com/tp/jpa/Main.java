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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        // TODO: Implementar submenú de Usuarios.
        // Opciones: 1-Alta  2-Modificar  3-Baja lógica  4-Listado  5-Buscar por mail  0-Volver
        System.out.println("[Usuarios] → TODO: implementar");
    }

    private static void menuPedidos() {
        // TODO: Implementar submenú de Pedidos.
        // Opciones: 1-Alta  2-Cambiar estado  3-Baja lógica  4-Listado
        //           5-Por usuario  6-Por estado  0-Volver
        System.out.println("[Pedidos] → TODO: implementar");
    }

    private static void menuReportes() {
        // TODO: Implementar submenú de Reportes.
        // Opciones: 1-Productos por categoría  2-Pedidos por usuario
        //           3-Pedidos por estado  4-Total facturado  0-Volver
        System.out.println("[Reportes] → TODO: implementar");
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

}
