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

let parent = null;
let parentId = null;
try { parent = shape.parent ?? null; } catch(e) {}
try { parentId = shape.parentId ?? shape.parent?.id ?? null; } catch(e) {}

const pid = parent?.id ?? parentId ?? null;

return JSON.stringify({ id: shape.id ?? null, parentId: pid });