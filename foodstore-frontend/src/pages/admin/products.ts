import { adminShell, wireChrome } from "../../components/layout";
import { getProductos } from "../../utils/storage";
import { formatPrecio } from "../../utils/format";
import { escapeHtml } from "../../utils/dom";

/** Renderiza la gestión de productos (panel admin). */
export function renderAdminProductos(app: HTMLElement): void {
  const productos = getProductos();

  const filas = productos
    .map((p) => {
      const estado = p.disponible
        ? `<span class="badge badge-success">Disponible</span>`
        : `<span class="badge badge-muted">No disponible</span>`;
      return `
      <tr data-id="${p.id}">
        <td>${p.id}</td>
        <td><div class="row-thumb" style="background-image:url('${escapeHtml(p.imagen)}')"></div></td>
        <td>${escapeHtml(p.nombre)}</td>
        <td>${escapeHtml(p.descripcion)}</td>
        <td>${formatPrecio(p.precio)}</td>
        <td>${escapeHtml(p.categoria.nombre)}</td>
        <td>${p.stock}</td>
        <td>${estado}</td>
      </tr>`;
    })
    .join("");

  app.innerHTML = adminShell(
    `
    <h2 class="page-title">Gestión de Productos</h2>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th><th>Imagen</th><th>Nombre</th><th>Descripción</th>
            <th>Precio</th><th>Categoría</th><th>Stock</th><th>Estado</th>
          </tr>
        </thead>
        <tbody>
          ${filas || `<tr><td colspan="8">No hay productos.</td></tr>`}
        </tbody>
      </table>
    </div>
  `,
    "productos"
  );
  wireChrome(app);

}
