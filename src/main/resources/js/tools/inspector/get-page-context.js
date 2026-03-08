const verbosity = ({{verbosity}} || "compact").toLowerCase();

const page = penpot.currentPage;
const all = page ? (page.findShapes?.({}) ?? []) : [];

const safeColor = (c) => c?.toString?.() ?? c ?? null;

const out = [];

for (const s of (Array.isArray(all) ? all : [])) {
  if (!s || !s.id) continue;

  const item = {
    id: s.id ?? null,
    name: s.name ?? null,
    type: s.type ?? null,
    x: s.x ?? null,
    y: s.y ?? null,
    width: s.width ?? null,
    height: s.height ?? null,
    fills: [],
    strokes: []
  };

  try {
    if (s.fills === "mixed") {
      item.fills = [{ color: "mixed", opacity: null, type: null }];
    } else if (Array.isArray(s.fills)) {
      item.fills = s.fills.map(f => ({
        color: safeColor(f?.fillColor ?? f?.color),
        opacity: f?.opacity ?? null,
        type: f?.type ?? null
      }));
    }
  } catch (e) {}

  try {
    if (Array.isArray(s.strokes)) {
      item.strokes = s.strokes.map(st => ({
        color: safeColor(st?.strokeColor ?? st?.color),
        opacity: st?.opacity ?? null,
        width: st?.width ?? null
      }));
    }
  } catch (e) {}

  if (verbosity === "full") {
    try { item.opacity = s.opacity ?? null; } catch(e) {}
    try { item.rotation = s.rotation ?? null; } catch(e) {}
  }

  out.push(item);
}

return JSON.stringify(out);