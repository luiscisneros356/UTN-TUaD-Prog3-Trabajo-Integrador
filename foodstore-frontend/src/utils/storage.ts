import type { Categoria, Producto, Pedido, Usuario } from "../types";

// Datos semilla: se cargan desde los JSON la primera vez y luego se trabaja
// sobre localStorage (persistencia entre sesiones, sólo con fines educativos).
import categoriasSeed from "../data/categorias.json";
import productosSeed from "../data/productos.json";
import usuariosSeed from "../data/usuarios.json";
import pedidosSeed from "../data/pedidos.json";

const KEYS = {
  categorias: "fs_categorias",
  productos: "fs_productos",
  usuarios: "fs_usuarios",
  pedidos: "fs_pedidos",
  seeded: "fs_seeded",
} as const;

function read<T>(key: string, fallback: T): T {
  const raw = localStorage.getItem(key);
  if (!raw) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function write<T>(key: string, value: T): void {
  localStorage.setItem(key, JSON.stringify(value));
}

/** Carga los datos semilla en localStorage la primera vez que se abre la app. */
export function seedData(): void {
  if (localStorage.getItem(KEYS.seeded)) return;
  write(KEYS.categorias, categoriasSeed);
  write(KEYS.productos, productosSeed);
  write(KEYS.usuarios, usuariosSeed);
  write(KEYS.pedidos, pedidosSeed);
  localStorage.setItem(KEYS.seeded, "1");
}

// ---- Categorías ----
export function getCategorias(): Categoria[] {
  return read<Categoria[]>(KEYS.categorias, []);
}
export function setCategorias(cats: Categoria[]): void {
  write(KEYS.categorias, cats);
}

// ---- Productos ----
export function getProductos(): Producto[] {
  return read<Producto[]>(KEYS.productos, []);
}
export function setProductos(prods: Producto[]): void {
  write(KEYS.productos, prods);
}
export function getProductoById(id: number): Producto | undefined {
  return getProductos().find((p) => p.id === id);
}

// ---- Usuarios ----
export function getUsuarios(): Usuario[] {
  return read<Usuario[]>(KEYS.usuarios, []);
}

// ---- Pedidos ----
export function getPedidos(): Pedido[] {
  return read<Pedido[]>(KEYS.pedidos, []);
}
export function setPedidos(pedidos: Pedido[]): void {
  write(KEYS.pedidos, pedidos);
}

/** Agrega un pedido nuevo asignándole un ID autoincremental. */
export function addPedido(pedido: Omit<Pedido, "id">): Pedido {
  const pedidos = getPedidos();
  const nuevoId = pedidos.reduce((max, p) => Math.max(max, p.id), 0) + 1;
  const nuevo: Pedido = { ...pedido, id: nuevoId };
  pedidos.push(nuevo);
  setPedidos(pedidos);
  return nuevo;
}
