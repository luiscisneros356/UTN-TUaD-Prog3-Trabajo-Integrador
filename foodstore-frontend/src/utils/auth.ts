import type { Usuario } from "../types";
import { getUsuarios } from "./storage";

const SESSION_KEY = "fs_session";

/** Resultado del intento de login. */
export interface LoginResult {
  ok: boolean;
  error?: string;
  usuario?: Usuario;
}

/** Valida credenciales contra los usuarios y guarda la sesión en localStorage. */
export function login(mail: string, password: string): LoginResult {
  const usuario = getUsuarios().find(
    (u) => u.mail.toLowerCase() === mail.trim().toLowerCase()
  );
  if (!usuario || usuario.password !== password) {
    return { ok: false, error: "Email o contraseña incorrectos." };
  }
  // No guardamos la contraseña en la sesión.
  const sesion: Usuario = { ...usuario };
  delete sesion.password;
  localStorage.setItem(SESSION_KEY, JSON.stringify(sesion));
  return { ok: true, usuario };
}

export function logout(): void {
  localStorage.removeItem(SESSION_KEY);
}

export function getCurrentUser(): Usuario | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Usuario;
  } catch {
    return null;
  }
}

export function isAuthenticated(): boolean {
  return getCurrentUser() !== null;
}

export function isAdmin(): boolean {
  return getCurrentUser()?.rol === "ADMIN";
}
