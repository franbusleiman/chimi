import { useEffect, useState } from "react";
import { api } from "../api/client";

type Block = {
  id?: number;
  startAt: string;
  endAt: string;
  reason?: string;
};

function startOfDay(d: Date) {
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

export default function Bloqueos() {
  const [items, setItems] = useState<Block[]>([]);
  const [draft, setDraft] = useState<Block>({ startAt: "", endAt: "", reason: "" });

  async function load() {
    const from = startOfDay(new Date()).toISOString();
    const to = new Date(Date.now() + 60 * 86400 * 1000).toISOString();
    const data = await api.get<Block[]>(`/blocks?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
    setItems(data);
  }

  useEffect(() => { load(); }, []);

  async function create() {
    await api.post("/blocks", {
      startAt: new Date(draft.startAt).toISOString(),
      endAt: new Date(draft.endAt).toISOString(),
      reason: draft.reason,
    });
    setDraft({ startAt: "", endAt: "", reason: "" });
    load();
  }

  async function del(id: number) {
    if (!confirm("¿Eliminar el bloqueo?")) return;
    await api.del(`/blocks/${id}`);
    load();
  }

  return (
    <div>
      <h2>Bloqueos de agenda</h2>
      <div className="card">
        <h3>Nuevo bloqueo</h3>
        <label><span>Desde</span><input type="datetime-local" value={draft.startAt} onChange={(e) => setDraft({ ...draft, startAt: e.target.value })} /></label>
        <label><span>Hasta</span><input type="datetime-local" value={draft.endAt} onChange={(e) => setDraft({ ...draft, endAt: e.target.value })} /></label>
        <label><span>Motivo</span><input value={draft.reason ?? ""} onChange={(e) => setDraft({ ...draft, reason: e.target.value })} placeholder="Vacaciones, capacitación, etc." /></label>
        <button className="btn" onClick={create} disabled={!draft.startAt || !draft.endAt}>Bloquear</button>
      </div>
      <div className="card">
        <table>
          <thead><tr><th>Desde</th><th>Hasta</th><th>Motivo</th><th></th></tr></thead>
          <tbody>
            {items.map((b) => (
              <tr key={b.id}>
                <td>{new Date(b.startAt).toLocaleString()}</td>
                <td>{new Date(b.endAt).toLocaleString()}</td>
                <td>{b.reason}</td>
                <td><button className="btn secondary" onClick={() => del(b.id!)}>Eliminar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
