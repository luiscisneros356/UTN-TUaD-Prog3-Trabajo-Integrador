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
│   ├── productos.json
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

## Instalación y requisitos previos

### 1. Requisitos

| Herramienta | Versión mínima | Verificar instalación |
|---|---|---|
| Java JDK | 23 | `java -version` |
| Gradle | 8 (incluido via wrapper) | no requiere instalación separada |
| Node.js | 18 | `node -v` |
| npm | 9 | `npm -v` |

> Gradle no necesita instalarse por separado: el proyecto incluye `gradlew.bat` (Windows) y `gradlew` (Linux/Mac), que descargan la versión correcta automáticamente.

### 2. Clonar el repositorio

```bash
git clone <URL-del-repositorio>
cd "Trabajo Integrador"
```

---

## Configuración de la base de datos

El backend usa **H2** como base de datos embebida en modo archivo. **No requiere ninguna instalación ni configuración manual.**

Al ejecutar el backend por primera vez, Hibernate crea automáticamente el archivo de base de datos en:

```
data/jpa_db.mv.db
```

La configuración de la conexión se encuentra en:

```
foodstore-backend/src/main/resources/META-INF/persistence.xml
```

Los parámetros relevantes son:

```xml
<!-- URL de la base de datos en archivo -->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:file:../data/jpa_db;AUTO_SERVER=FALSE"/>

<!-- Crea/actualiza el esquema automáticamente al arrancar -->
<property name="jakarta.persistence.schema-generation.database.action" value="update"/>
```

> **Advertencia benigna al arrancar:** Hibernate 6 puede mostrar `"H2Dialect does not need to be specified explicitly"`. Es normal y no afecta el funcionamiento.
>
> **Error de lock:** si aparece `"Database may be already in use"`, hay un proceso Java previo activo. Cerrarlo antes de volver a ejecutar.

---

## Cómo ejecutar el proyecto

### Backend (consola Java)

Abrir una terminal en la carpeta raíz del proyecto y ejecutar:

```bash
# Windows (recomendado)
cd foodstore-backend
gradlew.bat run

# Linux / Mac
cd foodstore-backend
./gradlew run
```

El menú principal aparecerá en consola. Desde allí se puede navegar por los submenús de categorías, productos, usuarios, pedidos y reportes.

**Primera ejecución:** la base de datos se crea sola. No hace falta cargar datos manualmente; los archivos JSON de `data/` son usados por el frontend. El backend permite crearlos desde consola.

---

### Frontend (SPA web)

Abrir una **segunda terminal** (el backend puede seguir corriendo) y ejecutar:

```bash
cd foodstore-frontend

# Instalar dependencias (solo la primera vez)
npm install

# Iniciar el servidor de desarrollo
npm run dev
```

Abrir el navegador en: **http://localhost:5173**

En el primer acceso, la aplicación carga automáticamente los datos de los archivos JSON de `data/` en `localStorage`. No hace falta configurar nada adicional.

#### Credenciales de acceso

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | admin@admin.com | 123456 |
| Cliente | cliente@food.com | cliente123 |

---

## Documentación Académica y Técnica (Formato PDF)

Se encuentra en el root del proyecto: `Documentacion_academica_tecnica.pdf`

---

## Video demostrativo

[Ver video en YouTube](ENLACE_AQUI)

---
