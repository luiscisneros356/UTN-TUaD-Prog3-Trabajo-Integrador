# Food Store — Trabajo Práctico Integrador

**Programación 3 · UTN**

Aplicación de e-commerce de comida desarrollada en dos partes independientes: un backend de consola con JPA/Hibernate y un frontend SPA con TypeScript y Vite.

---

## Descripción del proyecto

Food Store es un sistema de gestión de pedidos de comida que permite a los clientes explorar el catálogo, agregar productos al carrito y realizar pedidos; y a los administradores consultar el estado del negocio desde un panel de control.

El sistema está compuesto por:

- **Backend (consola):** aplicación Java que persiste datos con JPA/Hibernate sobre una base H2 en archivo. Expone menús interactivos para gestionar categorías, productos, usuarios y pedidos.
- **Frontend (SPA):** aplicación web de una sola página que consume los datos semilla en JSON y usa `localStorage` para simular sesión y carrito. No requiere conexión al backend para funcionar.

### Funcionalidades principales

| Área | Descripción |
|---|---|
| Autenticación | Login con validación de rol (ADMIN / USUARIO) |
| Catálogo | Listado de productos por categoría, vista de detalle |
| Carrito | Agregar/quitar productos, resumen de compra |
| Pedidos (cliente) | Historial de pedidos con detalle y estado |
| Panel admin | Dashboard de estadísticas, tablas de productos, pedidos y categorías |
| Backend consola | ABM de entidades, reportes por categoría/usuario/estado, total facturado |

---

## Arquitectura

```
Trabajo Integrador/
├── data/                  ← JSON de datos semilla (compartido)
│   ├── categorias.json
│   ├── productos.json     (generado por el frontend)
│   ├── usuarios.json
│   └── pedidos.json
├── foodstore-backend/     ← Java 23 · Gradle 8 · Hibernate 6 · H2
└── foodstore-frontend/    ← TypeScript 5 · Vite 5 · SPA vanilla
```

### Backend — stack tecnológico

- Java 23 / Gradle 8
- Hibernate 6.4 + Jakarta Persistence API 3.1
- H2 2.2 (base de datos en archivo `./data/jpa_db.mv.db`)
- Lombok
- Patrón Repository con `BaseRepository<T>` genérico
- Soft delete vía campo `eliminado` en entidad base

### Frontend — stack tecnológico

- TypeScript 5.4 / Vite 5.2
- SPA vanilla (sin framework)
- Router hash custom
- `localStorage` como store de sesión y carrito

---

## Cómo correrlo

### Requisitos previos

- **Java 23** o superior instalado y en el PATH
- **Node.js 18+** y **npm** instalados

---

### Backend (consola Java)

```bash
cd foodstore-backend

# Ejecutar directamente con Gradle (recomendado)
./gradlew run

# O en Windows
gradlew.bat run
```

La base de datos H2 se crea automáticamente en `./data/jpa_db.mv.db` al primer arranque.

> **Nota:** si aparece un error de lock al arrancar, hay un proceso Java anterior activo. Cerrarlo y volver a intentar.

---

### Frontend (SPA web)

```bash
cd foodstore-frontend

# Instalar dependencias (solo la primera vez)
npm install

# Levantar servidor de desarrollo
npm run dev
```

Abrir el navegador en `http://localhost:5173`.

#### Credenciales de acceso

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | admin@admin.com | 123456 |
| Cliente | cliente@food.com | cliente123 |

---

## Video demostrativo

[Ver video en YouTube](ENLACE_AQUI)

---
