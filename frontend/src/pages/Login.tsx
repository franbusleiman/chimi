import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/client";

export default function Login() {
  const [tenantSlug, setTenantSlug] = useState("demo");
  const [email, setEmail] = useState("admin@demo.chimi");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(tenantSlug, email, password);
      nav("/agenda");
    } catch (err: any) {
      setError("Credenciales inválidas");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-card card">
      <h2>Ingresar a Chimi</h2>
      <form onSubmit={submit}>
        <label>
          <span>Clínica</span>
          <input value={tenantSlug} onChange={(e) => setTenantSlug(e.target.value)} required />
        </label>
        <label>
          <span>Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          <span>Contraseña</span>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <div className="error">{error}</div>}
        <button className="btn" type="submit" disabled={loading} style={{ marginTop: "0.5rem", width: "100%" }}>
          {loading ? "Ingresando..." : "Ingresar"}
        </button>
      </form>
    </div>
  );
}
