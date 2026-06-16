import { adminShell, wireChrome } from "../../components/layout";
import { getProductos, setProductos } from "../../utils/storage";
import { formatPrecio } from "../../utils/format";
import { escapeHtml, toast } from "../../utils/dom";
import { refresh } from "../../utils/router";

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
        <td>
          <div class="table-actions">
            <button class="btn btn-outline" data-accion="editar">Editar</button>
            <button class="btn btn-danger" data-accion="eliminar">Eliminar</button>
          </div>
        </td>
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
            <th>Precio</th><th>Categoría</th><th>Stock</th><th>Estado</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${filas || `<tr><td colspan="9">No hay productos.</td></tr>`}
        </tbody>
      </table>
    </div>
  `,
    "productos"
  );
  wireChrome(app);

  app.querySelectorAll<HTMLTableRowElement>("tbody tr[data-id]").forEach((row) => {
    const id = Number(row.dataset.id);
    row.querySelector('[data-accion="editar"]')?.addEventListener("click", () => {
      const prod = getProductos().find((p) => p.id === id);
      if (!prod) return;
      const precioStr = prompt("Precio:", String(prod.precio));
      if (precioStr === null) return;
      const stockStr = prompt("Stock:", String(prod.stock));
      if (stockStr === null) return;
      const dispStr = prompt("¿Disponible? (s/n):", prod.disponible ? "s" : "n");
      if (dispStr === null) return;

      const precio = Number(precioStr);
      const stock = Number(stockStr);
      if (isNaN(precio) || precio <= 0) {
        toast("Precio inválido (debe ser mayor a 0).");
        return;
      }
      if (isNaN(stock) || stock < 0) {
        toast("Stock inválido (debe ser mayor o igual a 0).");
        return;
      }
      const todos = getProductos().map((p) =>
        p.id === id
          ? { ...p, precio, stock, disponible: dispStr.trim().toLowerCase() === "s" }
          : p
      );
      setProductos(todos);
      toast("Producto actualizado.");
      refresh();
    });
    row.querySelector('[data-accion="eliminar"]')?.addEventListener("click", () => {
      if (!confirm("¿Eliminar este producto?")) return;
      setProductos(getProductos().filter((p) => p.id !== id));
      toast("Producto eliminado.");
      refresh();
    });
  });
}
