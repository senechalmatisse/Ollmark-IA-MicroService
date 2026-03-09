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

const page = penpot.currentPage;
const all = page ? (page.findShapes?.({}) ?? []) : [];
const byId = new Map(Array.isArray(all) ? all.map(s => [s?.id, s]).filter(e => e[0]) : []);

let parent = null;
let parentId = null;

try { parent = shape.parent ?? null; } catch(e) {}
try { parentId = shape.parentId ?? shape.parent?.id ?? null; } catch(e) {}

if (!parent && parentId && byId.has(parentId)) parent = byId.get(parentId);

if (!parent) {
  return "Parent index of " + (shape.id ?? "null") + ": parentId=null, index=null (top-level)";
}

let children = [];
try {
  const ch = parent.children ?? parent.shapes ?? parent.items ?? null;
  children = Array.isArray(ch) ? ch : [];
} catch(e) { children = []; }

const idxApi = children.findIndex(c => (c?.id ?? null) === (shape.id ?? null));
const idxLayers = (idxApi >= 0) ? (children.length - 1 - idxApi) : -1;

return "Parent index of " + (shape.id ?? "null")
  + ": parentId=" + (parent.id ?? parentId ?? "null")
  + ", index=" + (idxApi >= 0 ? idxLayers : "null");