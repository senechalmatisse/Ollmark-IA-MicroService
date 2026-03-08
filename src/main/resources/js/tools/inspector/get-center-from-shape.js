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

let cx = null;
let cy = null;

try {
  if (shape.center && shape.center.x != null && shape.center.y != null) {
    cx = shape.center.x;
    cy = shape.center.y;
  } else {
    cx = (shape.x ?? 0) + ((shape.width ?? 0) / 2);
    cy = (shape.y ?? 0) + ((shape.height ?? 0) / 2);
  }
} catch (e) {}

return "Center of " + (shape.id ?? "unknown") + " is (" + cx + ", " + cy + ")";