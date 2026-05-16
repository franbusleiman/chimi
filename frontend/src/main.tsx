import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./styles.css";

// Vite expone BASE_URL con el valor del `base` que pusimos en vite.config.ts
// (siempre con trailing slash). React Router lo necesita SIN trailing slash.
const basename = (import.meta.env.BASE_URL || "/").replace(/\/+$/, "") || "/";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter basename={basename}>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);
