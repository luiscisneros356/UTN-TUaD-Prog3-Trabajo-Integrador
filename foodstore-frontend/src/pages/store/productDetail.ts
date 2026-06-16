import { storeShell, wireChrome } from "../../components/layout";
import type { RouteParams } from "../../utils/router";
import { getProductoById } from "../../utils/storage";
import { addToCart } from "../../utils/cart";
import { formatPrecio } from "../../utils/format";
import { escapeHtml, navigate, toast } from "../../utils/dom";

/** Renderiza el detalle de un producto. */
export function renderProductDetail(app: HTMLElement, params: RouteParams): void {
  const id = Number(params.id);
  const producto = getProductoById(id);

  if (!producto) {
    app.innerHTML = storeShell(
      `<div class="page"><div class="empty-state"><span class="emoji">❓</span>Producto no encontrado.
        <div style="margin-top:1rem"><a class="btn btn-primary" href="#/store">Volver a la tienda</a></div>
      </div></div>`,
      "store"
    );
    wireChrome(app);
    return;
  }

  const agotado = !producto.disponible || producto.stock <= 0;
  const estado = producto.disponible
    ? `<span class="badge badge-success">Disponible (Stock: ${producto.stock})</span>`
    : `<span class="badge badge-muted">No disponible</span>`;

  app.innerHTML = storeShell(
    `
    <div class="page">
      <div class="detail-wrap">
        <div class="detail-img" style="background-image:url('${escapeHtml(producto.imagen)}')"></div>
        <div class="detail-info">
          <span class="cat-label">${escapeHtml(producto.categoria.nombre)}</span>
          <h1>${escapeHtml(producto.nombre)}</h1>
          <div class="detail-price">${formatPrecio(producto.precio)}</div>
          ${estado}
          <p class="detail-desc">${escapeHtml(producto.descripcion)}</p>
          <div class="qty-selector">
            <span>Cantidad:</span>
            <button id="menos" type="button">−</button>
            <input type="number" id="cantidad" value="1" min="1" max="${producto.stock}" />
            <button id="mas" type="button">+</button>
          </div>
          <div class="detail-actions">
            <button class="btn btn-primary" id="agregar" ${agotado ? "disabled" : ""}>
              🛒 Agregar al Carrito
            </button>
            <button class="btn btn-outline" id="volver">← Volver</button>
          </div>
        </div>
      </div>
    </div>
  `,
    "store"
  );
  wireChrome(app);

  const inputCantidad = app.querySelector<HTMLInputElement>("#cantidad")!;

  function leerCantidad(): number {
    let val = parseInt(inputCantidad.value, 10);
    if (isNaN(val) || val < 1) val = 1;
    if (val > producto!.stock) val = producto!.stock;
    inputCantidad.value = String(val);
    return val;
  }

  app.querySelector<HTMLButtonElement>("#menos")!.addEventListener("click", () => {
    inputCantidad.value = String(Math.max(1, leerCantidad() - 1));
  });
  app.querySelector<HTMLButtonElement>("#mas")!.addEventListener("click", () => {
    inputCantidad.value = String(Math.min(producto.stock, leerCantidad() + 1));
  });
  inputCantidad.addEventListener("change", leerCantidad);

  app.querySelector<HTMLButtonElement>("#agregar")!.addEventListener("click", () => {
    if (agotado) {
      toast("El producto no está disponible.");
      return;
    }
    const cantidad = leerCantidad();
    const ok = addToCart(producto, cantidad);
    if (ok) {
      toast(`${producto.nombre} agregado al carrito.`);
      navigate("/store"); // refresca el badge del carrito al volver
    } else {
      toast(`No hay stock suficiente (disponible: ${producto.stock}).`);
    }
  });

  app.querySelector<HTMLButtonElement>("#volver")!.addEventListener("click", () => {
    navigate("/store");
  });
}
