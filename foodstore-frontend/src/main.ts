import "./style.css";
import { seedData } from "./utils/storage";
import {
  registerRoute,
  setNotFound,
  startRouter,
  type RouteHandler,
} from "./utils/router";
import { isAuthenticated, isAdmin } from "./utils/auth";
import { navigate } from "./utils/dom";

import { renderLogin } from "./pages/auth/login";
import { renderHome } from "./pages/store/home";
import { renderProductDetail } from "./pages/store/productDetail";
import { renderCart } from "./pages/store/cart";
import { renderMisPedidos } from "./pages/client/orders";
import { renderAdminDashboard } from "./pages/admin/dashboard";
import { renderAdminCategorias } from "./pages/admin/categories";
import { renderAdminProductos } from "./pages/admin/products";
import { renderAdminPedidos } from "./pages/admin/orders";

// Carga inicial de datos semilla en localStorage.
seedData();

/** Guard: exige sesión iniciada. */
function auth(handler: RouteHandler): RouteHandler {
  return (app, params) => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }
    handler(app, params);
  };
}

/** Guard: exige rol ADMIN. */
function soloAdmin(handler: RouteHandler): RouteHandler {
  return (app, params) => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }
    if (!isAdmin()) {
      navigate("/store");
      return;
    }
    handler(app, params);
  };
}

// Raíz: redirige según sesión/rol.
registerRoute("/", () => {
  if (!isAuthenticated()) navigate("/login");
  else if (isAdmin()) navigate("/admin");
  else navigate("/store");
});

registerRoute("/login", (app) => {
  if (isAuthenticated()) {
    navigate(isAdmin() ? "/admin" : "/store");
    return;
  }
  renderLogin(app);
});

// Cliente / tienda.
registerRoute("/store", auth(renderHome));
registerRoute("/producto/:id", auth(renderProductDetail));
registerRoute("/carrito", auth(renderCart));
registerRoute("/mis-pedidos", auth(renderMisPedidos));

// Administración.
registerRoute("/admin", soloAdmin(renderAdminDashboard));
registerRoute("/admin/categorias", soloAdmin(renderAdminCategorias));
registerRoute("/admin/productos", soloAdmin(renderAdminProductos));
registerRoute("/admin/pedidos", soloAdmin(renderAdminPedidos));

// Cualquier ruta desconocida vuelve a la raíz.
setNotFound(() => navigate("/"));

startRouter();
