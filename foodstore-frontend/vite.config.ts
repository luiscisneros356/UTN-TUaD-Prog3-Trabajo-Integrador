import { defineConfig } from 'vite';

// Base relativa para que el build funcione al abrirlo desde cualquier ruta.
export default defineConfig({
  base: './',
  server: {
    port: 5173,
    open: true,
  },
});
