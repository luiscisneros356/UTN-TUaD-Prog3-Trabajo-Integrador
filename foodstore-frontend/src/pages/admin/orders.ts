import type { EstadoPedido } from "../../types";
import { adminShell, wireChrome } from "../../components/layout";
import { getPedidos } from "../../utils/storage";
import { formatPrecio, formatFecha, estadoBadge } from "../../utils/format";
import { escapeHtml } from "../../utils/dom";
import { refresh } from "../../utils/router";

const ESTADOS: EstadoPedido[] = [
  "PENDIENTE",
  "EN_PREPARACION",
  "ENTREGADO",
  "CANCELADO",
];

const ESTADO_LABEL: Record<EstadoPedido, string> = {
  PENDIENTE: "Pendiente",
  EN_PREPARACION: "En Preparación",
  ENTREGADO: "Entregado",
  CANCELADO: "Cancelado",
};

let filtro: EstadoPedido | "TODOS" = "TODOS";

/** Renderiza la gestión de pedidos (panel admin): todos los pedidos + cambio de estado. */
export function renderAdminPedidos(app: HTMLElement): void {
  const pedidos = getPedidos();

  const opcionesFiltro = [
    `<option value="TODOS">Todos los pedidos</option>`,
    ...ESTADOS.map(
      (e) =>
        `<option value="${e}" ${filtro === e ? "selected" : ""}>${ESTADO_LABEL[e]}</option>`
    ),
  ].join("");

  const visibles = (filtro === "TODOS" ? pedidos : pedidos.filter((p) => p.estado === filtro))
    .slice()
    .sort((a, b) => {
      // Más recientes primero (por fecha y, a igualdad, por id).
      if (a.fecha === b.fecha) return b.id - a.id;
      return a.fecha < b.fecha ? 1 : -1;
    });

  const cards = visibles
    .map((p) => {
      const cliente = p.usuarioDto
        ? `${p.usuarioDto.nombre} ${p.usuarioDto.apellido}`
        : "(sin cliente)";
      return `
      <article class="order-card" style="cursor:default">
        <div class="order-head">
          <div>
            <div class="o-id">Pedido #${p.id}</div>
            <div class="o-date">Cliente: ${escapeHtml(cliente)}</div>
            <div class="o-date">📅 ${formatFecha(p.fecha)}</div>
          </div>
          ${estadoBadge(p.estado)}
        </div>
        <div class="order-foot">
          <span>${p.detalles.length} producto${p.detalles.length === 1 ? "" : "s"}</span>
          <span class="order-total">${formatPrecio(p.total)}</span>
        </div>
      </article>`;
    })
    .join("");

  app.innerHTML = adminShell(
    `
    <div class="admin-toolbar">
      <h2 class="page-title" style="margin:0">Gestión de Pedidos</h2>
      <select id="filtro">${opcionesFiltro}</select>
    </div>
    <div class="orders-list">
      ${cards || `<div class="empty-state"><span class="emoji">🧾</span>No hay pedidos${
        filtro === "TODOS" ? "" : " en este estado"
      }.</div>`}
    </div>
  `,
    "pedidos"
  );
  wireChrome(app);

  app.querySelector<HTMLSelectElement>("#filtro")!.addEventListener("change", (e) => {
    filtro = (e.target as HTMLSelectElement).value as EstadoPedido | "TODOS";
    refresh();
  });

}
