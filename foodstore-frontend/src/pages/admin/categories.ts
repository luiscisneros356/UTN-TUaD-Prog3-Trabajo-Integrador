import { adminShell, wireChrome } from "../../components/layout";
import { getCategorias } from "../../utils/storage";
import { escapeHtml } from "../../utils/dom";

/** Renderiza la gestión de categorías (panel admin). */
export function renderAdminCategorias(app: HTMLElement): void {
  const categorias = getCategorias();

  const filas = categorias
    .map(
      (c) => `
      <tr>
        <td>${c.id}</td>
        <td>${escapeHtml(c.nombre)}</td>
        <td>${escapeHtml(c.descripcion)}</td>
      </tr>`
    )
    .join("");

  app.innerHTML = adminShell(
    `
    <h2 class="page-title">Gestión de Categorías</h2>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Descripción</th></tr>
        </thead>
        <tbody>
          ${filas || `<tr><td colspan="3">No hay categorías.</td></tr>`}
        </tbody>
      </table>
    </div>
  `,
    "categorias"
  );
  wireChrome(app);
}
