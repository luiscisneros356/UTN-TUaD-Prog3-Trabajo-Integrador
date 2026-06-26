import { adminShell, wireChrome } from "../../components/layout";
import { getCategorias, getProductos, getPedidos } from "../../utils/storage";
import { formatPrecio } from "../../utils/format";

/** Renderiza el dashboard del panel de administración. */
export function renderAdminDashboard(app: HTMLElement): void {
  const categorias = getCategorias();
  const productos = getProductos();
  const pedidos = getPedidos();

  const disponibles = productos.filter((p) => p.disponible).length;
  const pendientes = pedidos.filter((p) => p.estado === "PENDIENTE").length;
  const enPreparacion = pedidos.filter((p) => p.estado === "EN_PREPARACION").length;
  const entregados = pedidos.filter((p) => p.estado === "ENTREGADO").length;
  const ingresos = pedidos
    .filter((p) => p.estado === "ENTREGADO")
    .reduce((s, p) => s + p.total, 0);

  app.innerHTML = adminShell(
    `
    <h2 class="page-title">Panel de Administración</h2>
    <div class="stat-grid">
      <div class="stat-card grad-purple">
        <div class="s-label">📁 Categorías</div>
        <div class="s-value">${categorias.length}</div>
        <a class="s-note" href="#/admin/categorias" style="color:#fff">Gestionar →</a>
      </div>
      <div class="stat-card grad-pink">
        <div class="s-label">🍔 Productos</div>
        <div class="s-value">${productos.length}</div>
        <a class="s-note" href="#/admin/productos" style="color:#fff">Gestionar →</a>
      </div>
      <div class="stat-card grad-cyan">
        <div class="s-label">🧾 Pedidos</div>
        <div class="s-value">${pedidos.length}</div>
        <a class="s-note" href="#/admin/pedidos" style="color:#fff">Gestionar →</a>
      </div>
      <div class="stat-card grad-green">
        <div class="s-label">✅ Disponibles</div>
        <div class="s-value">${disponibles}</div>
        <div class="s-note">Productos activos</div>
      </div>
    </div>

    <div class="panel">
      <h3>Resumen Rápido</h3>
      <div class="summary-grid">
        <div class="summary-box">
          <div class="sb-label">💰 Ingresos Totales</div>
          <div class="sb-value">${formatPrecio(ingresos)}</div>
        </div>
        <div class="summary-box">
          <div class="sb-label">⏳ Pedidos Pendientes</div>
          <div class="sb-value">${pendientes}</div>
        </div>
        <div class="summary-box">
          <div class="sb-label">🍳 En Preparación</div>
          <div class="sb-value">${enPreparacion}</div>
        </div>
        <div class="summary-box">
          <div class="sb-label">✅ Completados</div>
          <div class="sb-value">${entregados}</div>
        </div>
      </div>
    </div>
  `,
    "dashboard"
  );
  wireChrome(app);
}
