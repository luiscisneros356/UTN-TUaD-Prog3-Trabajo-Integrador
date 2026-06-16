import type { DetallePedido, FormaPago } from "../../types";
import { storeShell, wireChrome } from "../../components/layout";
import {
  getCart,
  updateCantidad,
  removeFromCart,
  clearCart,
  cartSubtotal,
  cartTotal,
  COSTO_ENVIO,
} from "../../utils/cart";
import { addPedido } from "../../utils/storage";
import { getCurrentUser } from "../../utils/auth";
import { formatPrecio } from "../../utils/format";
import { escapeHtml, navigate, toast } from "../../utils/dom";
import { refresh } from "../../utils/router";

/** Renderiza el carrito de compras. */
export function renderCart(app: HTMLElement): void {
  const items = getCart();

  if (items.length === 0) {
    app.innerHTML = storeShell(
      `<div class="page">
        <h2 class="page-title">Mi Carrito</h2>
        <div class="empty-state">
          <span class="emoji">🛒</span>
          Tu carrito está vacío.
          <div style="margin-top:1rem"><a class="btn btn-primary" href="#/store">Ir a la tienda</a></div>
        </div>
      </div>`,
      "carrito"
    );
    wireChrome(app);
    return;
  }

  const filas = items
    .map(
      (i) => `
      <div class="cart-item" data-id="${i.producto.id}">
        <div class="thumb" style="background-image:url('${escapeHtml(i.producto.imagen)}')"></div>
        <div>
          <div class="ci-name">${escapeHtml(i.producto.nombre)}</div>
          <div class="ci-desc">${escapeHtml(i.producto.descripcion)}</div>
          <div class="ci-controls">
            <button data-accion="menos">−</button>
            <span>${i.cantidad}</span>
            <button data-accion="mas">+</button>
            <span style="color:var(--text-soft);font-size:.8rem">× ${formatPrecio(i.producto.precio)}</span>
          </div>
        </div>
        <div class="ci-right">
          <span class="ci-total">${formatPrecio(i.producto.precio * i.cantidad)}</span>
          <button class="icon-btn" data-accion="quitar" title="Eliminar">🗑️</button>
        </div>
      </div>`
    )
    .join("");

  const subtotal = cartSubtotal();
  const total = cartTotal();

  app.innerHTML = storeShell(
    `
    <div class="page">
      <h2 class="page-title">Mi Carrito</h2>
      <div class="cart-layout">
        <div class="cart-items">${filas}</div>
        <aside class="cart-summary">
          <h3>Resumen del Pedido</h3>
          <div class="summary-row"><span>Subtotal</span><span>${formatPrecio(subtotal)}</span></div>
          <div class="summary-row"><span>Envío</span><span>${formatPrecio(COSTO_ENVIO)}</span></div>
          <div class="summary-row total"><span>Total</span><span>${formatPrecio(total)}</span></div>
          <div class="form-group" style="margin-top:1rem">
            <label for="forma">Forma de pago</label>
            <select id="forma" style="width:100%">
              <option value="TARJETA">Tarjeta</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="EFECTIVO">Efectivo</option>
            </select>
          </div>
          <p class="summary-note">Los pedidos se realizan directamente en el local.</p>
          <button class="btn btn-primary btn-block" id="confirmar" style="margin-bottom:.6rem">Confirmar Pedido</button>
          <button class="btn btn-outline btn-block" id="vaciar">Vaciar Carrito</button>
        </aside>
      </div>
    </div>
  `,
    "carrito"
  );
  wireChrome(app);

  // Controles de cantidad y eliminación.
  app.querySelectorAll<HTMLDivElement>(".cart-item").forEach((row) => {
    const id = Number(row.dataset.id);
    const item = items.find((i) => i.producto.id === id)!;
    row.querySelector('[data-accion="menos"]')?.addEventListener("click", () => {
      updateCantidad(id, item.cantidad - 1);
      refresh();
    });
    row.querySelector('[data-accion="mas"]')?.addEventListener("click", () => {
      if (item.cantidad + 1 > item.producto.stock) {
        toast(`Stock máximo alcanzado (${item.producto.stock}).`);
        return;
      }
      updateCantidad(id, item.cantidad + 1);
      refresh();
    });
    row.querySelector('[data-accion="quitar"]')?.addEventListener("click", () => {
      removeFromCart(id);
      refresh();
    });
  });

  app.querySelector<HTMLButtonElement>("#vaciar")!.addEventListener("click", () => {
    clearCart();
    toast("Carrito vaciado.");
    refresh();
  });

  app.querySelector<HTMLButtonElement>("#confirmar")!.addEventListener("click", () => {
    const usuario = getCurrentUser();
    if (!usuario) {
      navigate("/login");
      return;
    }
    const forma = app.querySelector<HTMLSelectElement>("#forma")!.value as FormaPago;
    const detalles: DetallePedido[] = items.map((i) => ({
      cantidad: i.cantidad,
      subtotal: i.producto.precio * i.cantidad,
      producto: i.producto,
    }));
    const hoy = new Date().toISOString().slice(0, 10);
    const pedido = addPedido({
      fecha: hoy,
      estado: "PENDIENTE",
      total: cartTotal(),
      formaPago: forma,
      detalles,
      usuarioDto: usuario,
    });
    clearCart();
    toast(`Pedido #${pedido.id} confirmado.`);
    navigate("/mis-pedidos");
  });
}
