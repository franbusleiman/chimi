import { useEffect, useState } from "react";
import { api } from "../api/client";

type Summary = {
  from: string;
  to: string;
  total: number;
  byStatus: Record<string, number>;
  byType: Record<string, number>;
  bySource: Record<string, number>;
  prepaidCount: number;
};

function isoDate(d: Date) {
  return d.toISOString().slice(0, 10);
}

export default function Reportes() {
  const today = new Date();
  const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  const [from, setFrom] = useState(isoDate(firstOfMonth));
  const [to, setTo] = useState(isoDate(today));
  const [data, setData] = useState<Summary | null>(null);

  async function load() {
    const d = await api.get<Summary>(`/reports/summary?from=${from}&to=${to}`);
    setData(d);
  }

  useEffect(() => { load(); }, [from, to]);

  return (
    <div>
      <div className="toolbar">
        <h2 style={{ margin: 0, marginRight: "auto" }}>Reportes</h2>
        <label style={{ margin: 0 }}><input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
        <label style={{ margin: 0 }}><input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
      </div>
      {data && (
        <>
          <div className="card">
            <h3>Total: {data.total} turnos</h3>
            <p>Pagados por adelantado: {data.prepaidCount}</p>
          </div>
          <div className="card">
            <h3>Por estado</h3>
            <table>
              <tbody>
                {Object.entries(data.byStatus).map(([k, v]) => <tr key={k}><td>{k}</td><td>{v}</td></tr>)}
              </tbody>
            </table>
          </div>
          <div className="card">
            <h3>Por tipo de turno</h3>
            <table>
              <tbody>
                {Object.entries(data.byType).map(([k, v]) => <tr key={k}><td>{k}</td><td>{v}</td></tr>)}
              </tbody>
            </table>
          </div>
          <div className="card">
            <h3>Por origen</h3>
            <table>
              <tbody>
                {Object.entries(data.bySource).map(([k, v]) => <tr key={k}><td>{k}</td><td>{v}</td></tr>)}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
