import { adminShell, wireChrome } from "../../components/layout";
import { getCategorias, setCategorias } from "../../utils/storage";
import { escapeHtml, toast } from "../../utils/dom";
import { refresh } from "../../utils/router";

/** Renderiza la gestión de categorías (panel admin). */
export function renderAdminCategorias(app: HTMLElement): void {
  const categorias = getCategorias();

  const filas = categorias
    .map(
      (c) => `
      <tr data-id="${c.id}">
        <td>${c.id}</td>
        <td>${escapeHtml(c.nombre)}</td>
        <td>${escapeHtml(c.descripcion)}</td>
        <td>
          <div class="table-actions">
            <button class="btn btn-outline" data-accion="editar">Editar</button>
            <button class="btn btn-danger" data-accion="eliminar">Eliminar</button>
          </div>
        </td>
      </tr>`
    )
    .join("");

  app.innerHTML = adminShell(
    `
    <h2 class="page-title">Gestión de Categorías</h2>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          ${filas || `<tr><td colspan="4">No hay categorías.</td></tr>`}
        </tbody>
      </table>
    </div>
  `,
    "categorias"
  );
  wireChrome(app);

  app.querySelectorAll<HTMLTableRowElement>("tbody tr[data-id]").forEach((row) => {
    const id = Number(row.dataset.id);
    row.querySelector('[data-accion="editar"]')?.addEventListener("click", () => {
      const cat = getCategorias().find((c) => c.id === id);
      if (!cat) return;
      const nombre = prompt("Nombre de la categoría:", cat.nombre);
      if (nombre === null) return;
      const descripcion = prompt("Descripción:", cat.descripcion);
      if (descripcion === null) return;
      const todas = getCategorias().map((c) =>
        c.id === id ? { ...c, nombre: nombre.trim() || c.nombre, descripcion } : c
      );
      setCategorias(todas);
      toast("Categoría actualizada.");
      refresh();
    });
    row.querySelector('[data-accion="eliminar"]')?.addEventListener("click", () => {
      if (!confirm("¿Eliminar esta categoría?")) return;
      setCategorias(getCategorias().filter((c) => c.id !== id));
      toast("Categoría eliminada.");
      refresh();
    });
  });
}
