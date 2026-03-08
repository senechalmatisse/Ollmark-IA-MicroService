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
} catch (e) {
  shape = null;
}

if (!shape) {
  return "Shape not found (shapeId=" + (inputId ?? "null") + ")";
}

const toHex = (c) => c?.toString?.() ?? c ?? null;
const colors = new Set();

let fillsState = "ok";

try {
  if (shape.fills === "mixed") {
    fillsState = "mixed";
  } else if (Array.isArray(shape.fills)) {
    if (shape.fills.length === 0) {
      fillsState = "empty";
    }
    for (const f of shape.fills) {
      const col = toHex(f?.fillColor ?? f?.color);
      if (col) colors.add(String(col));
    }
  } else {
    fillsState = "empty";
  }
} catch (e) {}

try {
  if (Array.isArray(shape.strokes)) {
    for (const st of shape.strokes) {
      const col = toHex(st?.strokeColor ?? st?.color);
      if (col) colors.add(String(col));
    }
  }
} catch (e) {}

return JSON.stringify({
  id: shape.id ?? null,
  fillsState,
  colors: Array.from(colors)
});