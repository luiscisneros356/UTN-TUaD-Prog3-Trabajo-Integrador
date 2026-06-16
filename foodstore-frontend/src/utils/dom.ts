/** Escapa texto para insertarlo de forma segura dentro de HTML. */
export function escapeHtml(value: unknown): string {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

let toastTimer: number | undefined;

/** Muestra un mensaje breve flotante (toast). */
export function toast(mensaje: string): void {
  let el = document.querySelector<HTMLDivElement>(".toast");
  if (!el) {
    el = document.createElement("div");
    el.className = "toast";
    document.body.appendChild(el);
  }
  el.textContent = mensaje;
  // Forzar reflow para reiniciar la animación.
  void el.offsetWidth;
  el.classList.add("show");
  if (toastTimer) window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => el?.classList.remove("show"), 2200);
}

/** Navega a una ruta por hash. */
export function navigate(path: string): void {
  window.location.hash = path;
}
