import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// El base path se setea en build-time vía la env var VITE_BASE_PATH
// (declarada como ARG/ENV en el Dockerfile).
//   "/"        → la SPA se sirve en el root del dominio
//   "/chimi/"  → la SPA se sirve bajo /chimi/  (path-based en un gateway compartido)
const basePath = process.env.VITE_BASE_PATH || "/";

export default defineConfig({
  base: basePath,
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
