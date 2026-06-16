// Router por hash muy simple. Soporta segmentos dinámicos (:id).

export type RouteParams = Record<string, string>;
export type RouteHandler = (app: HTMLElement, params: RouteParams) => void;

interface Route {
  pattern: string;
  parts: string[];
  handler: RouteHandler;
}

const routes: Route[] = [];
let notFound: RouteHandler | null = null;

export function registerRoute(pattern: string, handler: RouteHandler): void {
  routes.push({ pattern, parts: pattern.split("/").filter(Boolean), handler });
}

export function setNotFound(handler: RouteHandler): void {
  notFound = handler;
}

function matchRoute(path: string): { route: Route; params: RouteParams } | null {
  const segments = path.split("/").filter(Boolean);
  for (const route of routes) {
    if (route.parts.length !== segments.length) continue;
    const params: RouteParams = {};
    let ok = true;
    for (let i = 0; i < route.parts.length; i++) {
      const part = route.parts[i];
      const seg = segments[i];
      if (part.startsWith(":")) {
        params[part.slice(1)] = decodeURIComponent(seg);
      } else if (part !== seg) {
        ok = false;
        break;
      }
    }
    if (ok) return { route, params };
  }
  return null;
}

function resolve(): void {
  const app = document.getElementById("app");
  if (!app) return;
  const path = window.location.hash.replace(/^#/, "") || "/";
  const matched = matchRoute(path);
  window.scrollTo(0, 0);
  if (matched) {
    matched.route.handler(app, matched.params);
  } else if (notFound) {
    notFound(app, {});
  }
}

export function startRouter(): void {
  window.addEventListener("hashchange", resolve);
  resolve();
}

/** Re-renderiza la ruta actual (útil tras cambiar datos). */
export function refresh(): void {
  resolve();
}
