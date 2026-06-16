import type { ItemCarrito, Producto } from "../types";

const CART_KEY = "fs_cart";
const ENVIO = 500;

export const COSTO_ENVIO = ENVIO;

export function getCart(): ItemCarrito[] {
  const raw = localStorage.getItem(CART_KEY);
  if (!raw) return [];
  try {
    return JSON.parse(raw) as ItemCarrito[];
  } catch {
    return [];
  }
}

function saveCart(items: ItemCarrito[]): void {
  localStorage.setItem(CART_KEY, JSON.stringify(items));
}

/** Agrega un producto al carrito (suma cantidades si ya estaba), respetando stock. */
export function addToCart(producto: Producto, cantidad: number): boolean {
  const items = getCart();
  const existente = items.find((i) => i.producto.id === producto.id);
  const yaEnCarrito = existente ? existente.cantidad : 0;
  if (yaEnCarrito + cantidad > producto.stock) {
    return false;
  }
  if (existente) {
    existente.cantidad += cantidad;
    existente.producto = producto; // refresca datos (precio, etc.)
  } else {
    items.push({ producto, cantidad });
  }
  saveCart(items);
  return true;
}

/** Cambia la cantidad de un ítem; respeta el stock y elimina si llega a 0. */
export function updateCantidad(productoId: number, cantidad: number): void {
  const items = getCart();
  const item = items.find((i) => i.producto.id === productoId);
  if (!item) return;
  if (cantidad <= 0) {
    removeFromCart(productoId);
    return;
  }
  item.cantidad = Math.min(cantidad, item.producto.stock);
  saveCart(items);
}

export function removeFromCart(productoId: number): void {
  saveCart(getCart().filter((i) => i.producto.id !== productoId));
}

export function clearCart(): void {
  localStorage.removeItem(CART_KEY);
}

export function cartCount(): number {
  return getCart().reduce((sum, i) => sum + i.cantidad, 0);
}

export function cartSubtotal(): number {
  return getCart().reduce((sum, i) => sum + i.producto.precio * i.cantidad, 0);
}

export function cartTotal(): number {
  const sub = cartSubtotal();
  return sub > 0 ? sub + ENVIO : 0;
}
