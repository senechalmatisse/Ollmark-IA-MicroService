const inputId = {{shapeId}};
let shape = null;

try {
  if (!inputId || String(inputId).toLowerCase() === "selection") {
    const sel = penpot.selection ?? [];
    shape = (Array.isArray(sel) && sel.length > 0) ? sel[0] : null;
  } else {
    const page = penpot.currentPage;
    const all = page ? (page.findShapes?.({}) ?? []) : [];
    shape = Array.isArray(all) ? all.find(s => s && s.id === inputId) : null;
  }
} catch (e) { shape = null; }

if (!shape) return "Shape not found (shapeId=" + (inputId ?? "null") + ")";

let children = [];
try {
  const ch = shape.children ?? shape.shapes ?? shape.items ?? null;
  children = Array.isArray(ch) ? ch : [];
} catch (e) { children = []; }

const childrenIds = children.map(c => c?.id ?? null).filter(Boolean);

return JSON.stringify({ id: shape.id ?? null, childrenIds });