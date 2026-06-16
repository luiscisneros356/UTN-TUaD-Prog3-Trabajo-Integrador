import type { Producto } from "../../types";
import { storeShell, wireChrome } from "../../components/layout";
import { getCategorias, getProductos } from "../../utils/storage";
import { formatPrecio, } from "../../utils/format";
import { escapeHtml, navigate } from "../../utils/dom";

type Orden = "nombre-asc" | "nombre-desc" | "precio-asc" | "precio-desc";

/** Renderiza el catálogo de la tienda (home del cliente). */
export function renderHome(app: HTMLElement): void {
  const categorias = getCategorias();
  const productos = getProductos();

  let catSeleccionada: number | null = null;
  let busqueda = "";
  let orden: Orden = "nombre-asc";

  const sidebarItems = [
    `<li data-cat="" class="active">Todos los productos</li>`,
    ...categorias.map(
      (c) => `<li data-cat="${c.id}">${escapeHtml(c.nombre)}</li>`
    ),
  ].join("");

  app.innerHTML = storeShell(
    `
    <div class="store-layout">
      <aside class="sidebar">
        <h3>Categorías</h3>
        <p class="sidebar-sub">Filtrá por categoría</p>
        <ul class="cat-list">${sidebarItems}</ul>
      </aside>
      <div>
        <div class="store-toolbar">
          <input type="search" id="search" placeholder="🔍 Buscar productos..." />
          <select id="orden">
            <option value="nombre-asc">Nombre A-Z</option>
            <option value="nombre-desc">Nombre Z-A</option>
            <option value="precio-asc">Precio ↑</option>
            <option value="precio-desc">Precio ↓</option>
          </select>
        </div>
        <div class="store-head">
          <h2 class="page-title" id="head-title">Todos los Productos</h2>
          <span class="store-count" id="count"></span>
        </div>
        <div class="product-grid" id="grid"></div>
      </div>
    </div>
  `,
    "store"
  );
  wireChrome(app);

  const grid = app.querySelector<HTMLDivElement>("#grid")!;
  const count = app.querySelector<HTMLSpanElement>("#count")!;
  const headTitle = app.querySelector<HTMLHeadingElement>("#head-title")!;

  function filtrar(): Producto[] {
    let lista = productos.slice();
    if (catSeleccionada !== null) {
      lista = lista.filter((p) => p.categoria.id === catSeleccionada);
    }
    if (busqueda.trim()) {
      const q = busqueda.trim().toLowerCase();
      lista = lista.filter(
        (p) =>
          p.nombre.toLowerCase().includes(q) ||
          p.descripcion.toLowerCase().includes(q)
      );
    }
    lista.sort((a, b) => {
      switch (orden) {
        case "nombre-desc":
          return b.nombre.localeCompare(a.nombre);
        case "precio-asc":
          return a.precio - b.precio;
        case "precio-desc":
          return b.precio - a.precio;
        default:
          return a.nombre.localeCompare(b.nombre);
      }
    });
    return lista;
  }

  function pintar(): void {
    const lista = filtrar();
    count.textContent = `${lista.length} producto${lista.length === 1 ? "" : "s"}`;
    if (lista.length === 0) {
      grid.innerHTML = `<div class="empty-state"><span class="emoji">🔍</span>No se encontraron productos.</div>`;
      return;
    }
    grid.innerHTML = lista
      .map((p) => {
        const badge = p.disponible
          ? `<span class="badge badge-success">Disponible</span>`
          : `<span class="badge badge-muted">No disponible</span>`;
        return `
        <article class="product-card" data-id="${p.id}">
          <div class="thumb" style="background-image:url('${escapeHtml(p.imagen)}')"></div>
          <div class="card-body">
            <span class="cat-label">${escapeHtml(p.categoria.nombre)}</span>
            <span class="p-name">${escapeHtml(p.nombre)}</span>
            <span class="p-desc">${escapeHtml(p.descripcion)}</span>
            <div class="card-foot">
              <span class="price">${formatPrecio(p.precio)}</span>
              ${badge}
            </div>
          </div>
        </article>`;
      })
      .join("");

    grid.querySelectorAll<HTMLElement>(".product-card").forEach((card) => {
      card.addEventListener("click", () => navigate(`/producto/${card.dataset.id}`));
    });
  }

  // Eventos de filtros.
  app.querySelectorAll<HTMLLIElement>(".cat-list li").forEach((li) => {
    li.addEventListener("click", () => {
      app.querySelectorAll(".cat-list li").forEach((x) => x.classList.remove("active"));
      li.classList.add("active");
      const val = li.dataset.cat;
      catSeleccionada = val ? Number(val) : null;
      headTitle.textContent = val
        ? (categorias.find((c) => c.id === catSeleccionada)?.nombre ?? "Productos")
        : "Todos los Productos";
      pintar();
    });
  });

  app.querySelector<HTMLInputElement>("#search")!.addEventListener("input", (e) => {
    busqueda = (e.target as HTMLInputElement).value;
    pintar();
  });

  app.querySelector<HTMLSelectElement>("#orden")!.addEventListener("change", (e) => {
    orden = (e.target as HTMLSelectElement).value as Orden;
    pintar();
  });

  pintar();
}
