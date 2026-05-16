import { useEffect, useState } from "react";
import { api } from "../api/client";

type Appointment = {
  id: number;
  appointmentTypeName: string;
  petName: string;
  tutorName: string;
  tutorPhone: string;
  startAt: string;
  endAt: string;
  status: string;
  source: string;
  prepaid: boolean;
};

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default function Agenda() {
  const [date, setDate] = useState(todayIso());
  const [items, setItems] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await api.get<Appointment[]>(`/appointments?date=${date}`);
      setItems(data);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [date]);

  async function cancel(id: number) {
    if (!confirm("¿Cancelar este turno?")) return;
    await api.post(`/appointments/${id}/cancel`, { reason: "cancelado desde dashboard" });
    load();
  }

  async function attended(id: number) {
    await api.post(`/appointments/${id}/attended`);
    load();
  }

  return (
    <div>
      <div className="toolbar">
        <h2 style={{ margin: 0, marginRight: "auto" }}>Agenda</h2>
        <label style={{ margin: 0 }}>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </label>
      </div>
      <div className="card">
        {loading && <p>Cargando...</p>}
        {error && <p className="error">{error}</p>}
        {!loading && items.length === 0 && <p>Sin turnos para esta fecha.</p>}
        {items.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Hora</th>
                <th>Tipo</th>
                <th>Mascota</th>
                <th>Tutor</th>
                <th>Teléfono</th>
                <th>Origen</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.startAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</td>
                  <td>{a.appointmentTypeName}</td>
                  <td>{a.petName}</td>
                  <td>{a.tutorName}</td>
                  <td>{a.tutorPhone}</td>
                  <td>{a.source}</td>
                  <td>{a.status}{a.prepaid ? " · pago ✓" : ""}</td>
                  <td>
                    {a.status !== "ATTENDED" && a.status !== "CANCELLED" && (
                      <>
                        <button className="btn" onClick={() => attended(a.id)}>Atendido</button>{" "}
                        <button className="btn secondary" onClick={() => cancel(a.id)}>Cancelar</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
