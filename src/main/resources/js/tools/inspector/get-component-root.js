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

const chain = [];
let current = shape;
let depth = 0;

while (current) {
  chain.push({
    id: current.id ?? null,
    name: current.name ?? null,
    type: current.type ?? null
  });

  let parent = null;
  try { parent = current.parent ?? null; } catch(e) {}

  if (!parent) {
    let pid = null;
    try { pid = current.parentId ?? current.parent?.id ?? null; } catch(e) {}
    if (pid && byId.has(pid)) parent = byId.get(pid);
  }

  if (!parent) break;
  current = parent;
  depth++;
  if (depth > 50) break;
}

const root = chain.length ? chain[chain.length - 1] : null;

const chainStr = chain
  .map((n, i) => {
    const label = (n.name ?? "Unnamed") + ":" + (n.type ?? "?");
    return (i === 0 ? "self" : ("p" + i)) + "=" + (n.id ?? "null") + " (" + label + ")";
  })
  .join(" -> ");

return "Root of " + (shape.id ?? "null")
  + " is " + (root?.id ?? "null")
  + " (" + (root?.name ?? "Unnamed") + ", " + (root?.type ?? "?") + ")"
  + " | depth=" + depth
  + " | chain: " + chainStr;