import { useEffect, useState } from "react";
import { api } from "../api/client";

type Faq = {
  id?: number;
  category: "CLINIC" | "PRODUCTS";
  question: string;
  answer: string;
  displayOrder: number;
  active: boolean;
};

const empty: Faq = { category: "CLINIC", question: "", answer: "", displayOrder: 0, active: true };

export default function Faqs() {
  const [items, setItems] = useState<Faq[]>([]);
  const [draft, setDraft] = useState<Faq>(empty);
  const [editingId, setEditingId] = useState<number | null>(null);

  async function load() {
    const data = await api.get<Faq[]>("/faqs");
    setItems(data);
  }

  useEffect(() => { load(); }, []);

  async function save() {
    if (editingId) await api.put(`/faqs/${editingId}`, draft);
    else await api.post("/faqs", draft);
    setDraft(empty);
    setEditingId(null);
    load();
  }

  async function del(id: number) {
    if (!confirm("¿Eliminar esta FAQ?")) return;
    await api.del(`/faqs/${id}`);
    load();
  }

  return (
    <div>
      <h2>Preguntas frecuentes</h2>
      <div className="card">
        <table>
          <thead><tr><th>Categoría</th><th>Pregunta</th><th>Activa</th><th></th></tr></thead>
          <tbody>
            {items.map((f) => (
              <tr key={f.id}>
                <td>{f.category}</td>
                <td>{f.question}</td>
                <td>{f.active ? "Sí" : "No"}</td>
                <td>
                  <button className="btn secondary" onClick={() => { setDraft(f); setEditingId(f.id ?? null); }}>Editar</button>{" "}
                  <button className="btn secondary" onClick={() => del(f.id!)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="card">
        <h3>{editingId ? "Editar FAQ" : "Nueva FAQ"}</h3>
        <label><span>Categoría</span>
          <select value={draft.category} onChange={(e) => setDraft({ ...draft, category: e.target.value as Faq["category"] })}>
            <option value="CLINIC">Clínica</option>
            <option value="PRODUCTS">Productos</option>
          </select>
        </label>
        <label><span>Pregunta</span><input value={draft.question} onChange={(e) => setDraft({ ...draft, question: e.target.value })} /></label>
        <label><span>Respuesta</span><textarea rows={4} value={draft.answer} onChange={(e) => setDraft({ ...draft, answer: e.target.value })} /></label>
        <label><span>Orden</span><input type="number" value={draft.displayOrder} onChange={(e) => setDraft({ ...draft, displayOrder: Number(e.target.value) })} /></label>
        <label><span>Activa</span>
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
