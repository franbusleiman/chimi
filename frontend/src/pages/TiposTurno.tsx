import { useEffect, useState } from "react";
import { api } from "../api/client";

type AppointmentType = {
  id?: number;
  code: string;
  name: string;
  durationMinutes: number;
  description?: string;
  active: boolean;
  displayOrder: number;
};

const empty: AppointmentType = {
  code: "",
  name: "",
  durationMinutes: 30,
  description: "",
  active: true,
  displayOrder: 0,
};

export default function TiposTurno() {
  const [items, setItems] = useState<AppointmentType[]>([]);
  const [draft, setDraft] = useState<AppointmentType>(empty);
  const [editingId, setEditingId] = useState<number | null>(null);

  async function load() {
    const data = await api.get<AppointmentType[]>("/appointment-types");
    setItems(data);
  }

  useEffect(() => {
    load();
  }, []);

  async function save() {
    if (editingId) await api.put(`/appointment-types/${editingId}`, draft);
    else await api.post("/appointment-types", draft);
    setDraft(empty);
    setEditingId(null);
    load();
  }

  function edit(t: AppointmentType) {
    setDraft(t);
    setEditingId(t.id ?? null);
  }

  async function del(id: number) {
    if (!confirm("¿Desactivar este tipo?")) return;
    await api.del(`/appointment-types/${id}`);
    load();
  }

  return (
    <div>
      <h2>Tipos de turno</h2>
      <div className="card">
        <table>
          <thead>
            <tr><th>Orden</th><th>Código</th><th>Nombre</th><th>Duración</th><th>Activo</th><th></th></tr>
          </thead>
          <tbody>
            {items.map((t) => (
              <tr key={t.id}>
                <td>{t.displayOrder}</td>
                <td>{t.code}</td>
                <td>{t.name}</td>
                <td>{t.durationMinutes} min</td>
                <td>{t.active ? "Sí" : "No"}</td>
                <td>
                  <button className="btn secondary" onClick={() => edit(t)}>Editar</button>{" "}
                  <button className="btn secondary" onClick={() => del(t.id!)}>Desactivar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="card">
        <h3>{editingId ? "Editar tipo" : "Nuevo tipo"}</h3>
        <label><span>Código</span><input value={draft.code} onChange={(e) => setDraft({ ...draft, code: e.target.value })} /></label>
        <label><span>Nombre</span><input value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} /></label>
        <label><span>Duración (min)</span><input type="number" value={draft.durationMinutes} onChange={(e) => setDraft({ ...draft, durationMinutes: Number(e.target.value) })} /></label>
        <label><span>Orden</span><input type="number" value={draft.displayOrder} onChange={(e) => setDraft({ ...draft, displayOrder: Number(e.target.value) })} /></label>
        <label><span>Descripción</span><textarea value={draft.description ?? ""} onChange={(e) => setDraft({ ...draft, description: e.target.value })} /></label>
        <label><span>Activo</span>
          <select value={draft.active ? "1" : "0"} onChange={(e) => setDraft({ ...draft, active: e.target.value === "1" })}>
            <option value="1">Sí</option><option value="0">No</option>
          </select>
        </label>
        <button className="btn" onClick={save}>{editingId ? "Guardar" : "Crear"}</button>
        {editingId && <button className="btn secondary" style={{ marginLeft: 8 }} onClick={() => { setDraft(empty); setEditingId(null); }}>Cancelar</button>}
      </div>
    </div>
  );
}
