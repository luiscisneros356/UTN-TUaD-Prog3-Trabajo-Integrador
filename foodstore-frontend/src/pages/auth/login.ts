import { login } from "../../utils/auth";
import { navigate } from "../../utils/dom";

/** Renderiza la pantalla de login. */
export function renderLogin(app: HTMLElement): void {
  app.innerHTML = `
    <div class="login-screen">
      <div class="login-card">
        <h1>Food Store</h1>
        <p class="subtitle">Iniciar Sesión</p>
        <div class="login-error" id="login-error" style="display:none"></div>
        <form id="login-form">
          <div class="form-group">
            <label for="mail">Email</label>
            <input type="email" id="mail" placeholder="ejemplo@correo.com" autocomplete="username" />
          </div>
          <div class="form-group">
            <label for="password">Contraseña</label>
            <input type="password" id="password" placeholder="Ingresa tu contraseña" autocomplete="current-password" />
          </div>
          <button type="submit" class="btn btn-primary btn-block">Ingresar</button>
        </form>
      </div>
    </div>
  `;

  const form = app.querySelector<HTMLFormElement>("#login-form")!;
  const errorBox = app.querySelector<HTMLDivElement>("#login-error")!;

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const mail = app.querySelector<HTMLInputElement>("#mail")!.value.trim();
    const password = app.querySelector<HTMLInputElement>("#password")!.value;

    if (!mail || !password) {
      errorBox.textContent = "Completá email y contraseña.";
      errorBox.style.display = "block";
      return;
    }

    const result = login(mail, password);
    if (!result.ok) {
      errorBox.textContent = result.error ?? "No se pudo iniciar sesión.";
      errorBox.style.display = "block";
      return;
    }

    // Redirección según rol.
    if (result.usuario?.rol === "ADMIN") {
      navigate("/admin");
    } else {
      navigate("/store");
    }
  });
}
