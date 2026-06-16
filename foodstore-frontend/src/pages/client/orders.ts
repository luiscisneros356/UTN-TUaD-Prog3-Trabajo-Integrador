import type { Pedido, EstadoPedido } from "../../types";
import { storeShell, wireChrome } from "../../components/layout";
import { getCurrentUser } from "../../utils/auth";
import { getPedidos } from "../../utils/storage";
import { formatPrecio, formatFecha, estadoBadge, estadoInfo } from "../../utils/format";
import { escapeHtml } from "../../utils/dom";

const FORMA_LABEL: Record<string, string> = {
  TARJETA: "Tarjeta",
  TRANSFERENCIA: "Transferencia",
  EFECTIVO: "Efectivo",
};

/** Renderiza el historial de pedidos del cliente autenticado. */
export function renderMisPedidos(app: HTMLElement): void {
  const usuario = getCurrentUser();
  const propios = getPedidos().filter((p) => p.usuarioDto?.id === usuario?.id);

  let filtro: EstadoPedido | "TODOS" = "TODOS";

  app.innerHTML = storeShell(
    `
    <div class="page">
      <div class="store-head">
        <h2 class="page-title">Mis Pedidos</h2>
        <select id="filtro">
          <option value="TODOS">Todos</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="EN_PREPARACION">En Preparación</option>
          <option value="ENTREGADO">Entregado</option>
          <option value="CANCELADO">Cancelado</option>
        </select>
      </div>
      <div id="lista" class="orders-list"></div>
    </div>
  `,
    "pedidos"
  );
  wireChrome(app);

  const lista = app.querySelector<HTMLDivElement>("#lista")!;

  function pintar(): void {
    const visibles =
      filtro === "TODOS" ? propios : propios.filter((p) => p.estado === filtro);

    if (visibles.length === 0) {
      lista.innerHTML = `<div class="empty-state"><span class="emoji">📦</span>No tenés pedidos${
        filtro === "TODOS" ? "" : " en este estado"
      }.</div>`;
      return;
    }

    // Más recientes primero.
    const ordenados = visibles.slice().sort((a, b) => b.id - a.id);

    lista.innerHTML = ordenados
      .map((p) => {
        const nombres = p.detalles.map((d) => `${d.producto.nombre} (x${d.cantidad})`);
        const primeros = nombres.slice(0, 3).join(", ");
        const extra = nombres.length > 3 ? ` y ${nombres.length - 3} más` : "";
        return `
        <article class="order-card" data-id="${p.id}">
          <div class="order-head">
            <div>
              <div class="o-id">Pedido #${p.id}</div>
              <div class="o-date">📅 ${formatFecha(p.fecha)}</div>
            </div>
            ${estadoBadge(p.estado)}
          </div>
          <div class="order-products">${escapeHtml(primeros)}${escapeHtml(extra)}</div>
          <div class="order-foot">
            <span>${p.detalles.length} producto${p.detalles.length === 1 ? "" : "s"}</span>
            <span class="order-total">${formatPrecio(p.total)}</span>
          </div>
        </article>`;
      })
      .join("");

    lista.querySelectorAll<HTMLElement>(".order-card").forEach((card) => {
      card.addEventListener("click", () => {
        const pedido = propios.find((p) => p.id === Number(card.dataset.id));
        if (pedido) abrirModal(pedido);
      });
    });
  }

  function abrirModal(pedido: Pedido): void {
    const info = estadoInfo(pedido.estado);
    const subtotal = pedido.detalles.reduce((s, d) => s + d.subtotal, 0);
    const envio = Math.max(0, pedido.total - subtotal);
    const tel = pedido.usuarioDto?.celular ?? "-";
    const productos = pedido.detalles
      .map(
        (d) => `
        <div class="modal-line">
          <span>${escapeHtml(d.producto.nombre)} <small>x${d.cantidad}</small></span>
          <span>${formatPrecio(d.subtotal)}</span>
        </div>`
      )
      .join("");

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.innerHTML = `
      <div class="modal">
        <div class="modal-head">
          <h3>${info.icon} Pedido #${pedido.id} — ${info.label}</h3>
          <button class="modal-close" aria-label="Cerrar">×</button>
        </div>
        <div class="modal-section">
          <h4>📍 Información de Entrega</h4>
          <div class="modal-line"><span>Teléfono:</span><span>${escapeHtml(tel)}</span></div>
          <div class="modal-line"><span>Método de pago:</span><span>${
            FORMA_LABEL[pedido.formaPago] ?? pedido.formaPago
          }</span></div>
          <div class="modal-line"><span>Fecha:</span><span>${formatFecha(pedido.fecha)}</span></div>
        </div>
        <div class="modal-section">
          <h4>🍔 Productos</h4>
          ${productos}
        </div>
        <div class="modal-section">
          <div class="modal-line"><span>Subtotal</span><span>${formatPrecio(subtotal)}</span></div>
          <div class="modal-line"><span>Envío</span><span>${formatPrecio(envio)}</span></div>
          <div class="modal-line" style="font-weight:700"><span>Total</span><span>${formatPrecio(
            pedido.total
          )}</span></div>
        </div>
        <div class="status-message">${info.icon} ${info.mensaje}</div>
      </div>
    `;
    document.body.appendChild(overlay);

    const cerrar = () => overlay.remove();
    overlay.querySelector(".modal-close")?.addEventListener("click", cerrar);
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) cerrar();
    });
  }

  app.querySelector<HTMLSelectElement>("#filtro")!.addEventListener("change", (e) => {
    filtro = (e.target as HTMLSelectElement).value as EstadoPedido | "TODOS";
    pintar();
  });

  pintar();
}
