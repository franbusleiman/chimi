import { NavLink, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { clearSession, getUser } from "./api/client";
import Login from "./pages/Login";
import Agenda from "./pages/Agenda";
import Bloqueos from "./pages/Bloqueos";
import TiposTurno from "./pages/TiposTurno";
import Faqs from "./pages/Faqs";
import Reportes from "./pages/Reportes";

function RequireAuth({ children }: { children: JSX.Element }) {
  const user = getUser();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

function Layout({ children }: { children: React.ReactNode }) {
  const user = getUser();
  const nav = useNavigate();
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>🐾 Chimi</h1>
        <div style={{ fontSize: "0.8rem", opacity: 0.8, marginBottom: "1rem" }}>
          {user?.fullName}
        </div>
        <NavLink to="/agenda" className={({ isActive }) => (isActive ? "active" : "")}>Agenda</NavLink>
        <NavLink to="/tipos" className={({ isActive }) => (isActive ? "active" : "")}>Tipos de turno</NavLink>
        <NavLink to="/bloqueos" className={({ isActive }) => (isActive ? "active" : "")}>Bloqueos</NavLink>
        <NavLink to="/faqs" className={({ isActive }) => (isActive ? "active" : "")}>FAQs</NavLink>
        <NavLink to="/reportes" className={({ isActive }) => (isActive ? "active" : "")}>Reportes</NavLink>
        <button
          className="btn secondary"
          style={{ marginTop: "1rem", width: "100%" }}
          onClick={() => {
            clearSession();
            nav("/login");
          }}
        >
          Salir
        </button>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <Layout>
              <Navigate to="/agenda" replace />
            </Layout>
          </RequireAuth>
        }
      />
      <Route path="/agenda" element={<RequireAuth><Layout><Agenda /></Layout></RequireAuth>} />
      <Route path="/tipos" element={<RequireAuth><Layout><TiposTurno /></Layout></RequireAuth>} />
      <Route path="/bloqueos" element={<RequireAuth><Layout><Bloqueos /></Layout></RequireAuth>} />
      <Route path="/faqs" element={<RequireAuth><Layout><Faqs /></Layout></RequireAuth>} />
      <Route path="/reportes" element={<RequireAuth><Layout><Reportes /></Layout></RequireAuth>} />
      <Route path="*" element={<Navigate to="/agenda" replace />} />
    </Routes>
  );
}
