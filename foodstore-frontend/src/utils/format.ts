import type { EstadoPedido } from "../types";

/** Formatea un número como precio en pesos: $4500.00 */
export function formatPrecio(valor: number): string {
  return "$" + valor.toFixed(2);
}

/** Formatea una fecha ISO (yyyy-mm-dd) a un texto legible en español. */
export function formatFecha(iso: string): string {
  const partes = iso.split("-");
  if (partes.length !== 3) return iso;
  const [anio, mes, dia] = partes;
  const meses = [
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
  ];
  const nombreMes = meses[Number(mes) - 1] ?? mes;
  return `${Number(dia)} de ${nombreMes} de ${anio}`;
}

interface EstadoInfo {
  label: string;
  icon: string;
  badge: string; // clase de badge
  mensaje: string;
}

const ESTADOS: Record<EstadoPedido, EstadoInfo> = {
  PENDIENTE: {
    label: "Pendiente",
    icon: "⏳",
    badge: "badge-warning",
    mensaje: "Tu pedido está pendiente. Te avisaremos cuando sea confirmado.",
  },
  EN_PREPARACION: {
    label: "En Preparación",
    icon: "🍳",
    badge: "badge-info",
    mensaje: "Tu pedido se está preparando en este momento.",
  },
  ENTREGADO: {
    label: "Entregado",
    icon: "✅",
    badge: "badge-success",
    mensaje: "Tu pedido fue entregado. ¡Que lo disfrutes!",
  },
  CANCELADO: {
    label: "Cancelado",
    icon: "❌",
    badge: "badge-danger",
    mensaje: "Este pedido fue cancelado.",
  },
};

export function estadoInfo(estado: EstadoPedido): EstadoInfo {
  return ESTADOS[estado] ?? ESTADOS.PENDIENTE;
}

/** Devuelve el HTML de un badge de estado de pedido. */
export function estadoBadge(estado: EstadoPedido): string {
  const info = estadoInfo(estado);
  return `<span class="badge ${info.badge}">${info.label}</span>`;
}
