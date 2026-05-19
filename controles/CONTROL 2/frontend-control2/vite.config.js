import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: true, // necesario para que Docker pueda acceder
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080", // en local; en Docker se sobreescribe con env
        changeOrigin: true,
      },
    },
  },
});
