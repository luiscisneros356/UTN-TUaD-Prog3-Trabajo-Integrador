import { getCurrentUser, logout } from "../utils/auth";
import { cartCount } from "../utils/cart";
import { escapeHtml, navigate } from "../utils/dom";

type StoreLink = "store" | "pedidos" | "carrito" | "";

/** Envuelve el contenido con la navbar de la tienda (cliente). */
export function storeShell(contentHtml: string, active: StoreLink = ""): string {
  const user = getCurrentUser();
  const nombre = user ? `${user.nombre} ${user.apellido}` : "";
  const count = cartCount();
  const badge = count > 0 ? `<span class="cart-count">${count}</span>` : "";
  return `
    <nav class="navbar">
      <div class="navbar-inner">
        <a class="navbar-brand" href="#/store">🍔 Food Store</a>
        <div class="navbar-links">
          <a href="#/store" class="${active === "store" ? "active" : ""}">Inicio</a>
          <a href="#/mis-pedidos" class="${active === "pedidos" ? "active" : ""}">Mis Pedidos</a>
          <a href="#/carrito" class="cart-link ${active === "carrito" ? "active" : ""}">🛒 Carrito ${badge}</a>
          <span class="navbar-user">${escapeHtml(nombre)}</span>
          <button class="btn btn-outline" data-logout>Cerrar Sesión</button>
        </div>
      </div>
    </nav>
    <main>${contentHtml}</main>
  `;
}

type AdminKey = "dashboard" | "categorias" | "productos" | "pedidos";

/** Envuelve el contenido con la navbar superior y el sidebar del panel admin. */
export function adminShell(contentHtml: string, active: AdminKey): string {
  const user = getCurrentUser();
  const nombre = user ? `${user.nombre} ${user.apellido}` : "";
  const item = (key: AdminKey | "tienda", href: string, label: string) =>
    `<li><a href="${href}" class="${active === key ? "active" : ""}">${label}</a></li>`;
  return `
    <nav class="navbar">
      <div class="navbar-inner">
        <a class="navbar-brand" href="#/admin">🍔 Food Store</a>
        <div class="navbar-links">
          <a href="#/store">Tienda</a>
          <a href="#/admin" class="active">Panel Admin</a>
          <span class="navbar-user">${escapeHtml(nombre)}</span>
          <button class="btn btn-outline" data-logout>Cerrar Sesión</button>
        </div>
      </div>
    </nav>
    <div class="admin-layout">
      <aside class="admin-sidebar">
        <div class="as-title">Administración</div>
        <div class="as-sub">Panel de control</div>
        <ul class="admin-nav">
          ${item("dashboard", "#/admin", "📊 Dashboard")}
          ${item("categorias", "#/admin/categorias", "📁 Categorías")}
          ${item("productos", "#/admin/productos", "🍔 Productos")}
          ${item("pedidos", "#/admin/pedidos", "🧾 Pedidos")}
          ${item("tienda", "#/store", "🏪 Ver Tienda")}
        </ul>
      </aside>
      <section class="admin-content">${contentHtml}</section>
    </div>
  `;
}

/** Conecta los botones comunes (cerrar sesión) después de renderizar. */
export function wireChrome(app: HTMLElement): void {
  app.querySelector<HTMLButtonElement>("[data-logout]")?.addEventListener("click", () => {
    logout();
    navigate("/login");
  });
}
