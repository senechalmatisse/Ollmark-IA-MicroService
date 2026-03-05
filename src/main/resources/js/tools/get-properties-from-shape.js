const inputId = {{shapeId}};
const verbosity = ({{verbosity}} ?? "compact").toString().trim().toLowerCase();

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

/* --- text handling (unit test requires type === "text") --- */
let textContent = null;
try {
  const type = (shape.type ?? "").toString().toLowerCase();
  if (type === "text") {
    textContent = shape.content ?? shape.text ?? null;
  }
} catch (e) {}

/* --- fills --- */
let fillsSummary = [];
let fillsState = "empty";

try {
  if (shape.fills === "mixed") {
    fillsState = "mixed";
  } else if (Array.isArray(shape.fills) && shape.fills.length) {
    fillsState = "ok";
    for (const f of shape.fills) {
      fillsSummary.push({
        color: f?.fillColor ?? f?.color ?? null,
        opacity: f?.opacity ?? null
      });
    }
  }
} catch (e) {}

/* --- strokes --- */
let strokesSummary = [];
let strokesState = "empty";

try {
  if (shape.strokes === "mixed") {
    strokesState = "mixed";
  } else if (Array.isArray(shape.strokes) && shape.strokes.length) {
    strokesState = "ok";
    for (const s of shape.strokes) {
      strokesSummary.push({
        color: s?.strokeColor ?? s?.color ?? null,
        width: s?.strokeWidth ?? s?.width ?? null
      });
    }
  }
} catch (e) {}

/* --- normalize verbosity --- */
let v = verbosity;
if (!v) v = "compact";
if (v !== "compact" && v !== "full") v = "compact";

if (v === "compact") {
  return JSON.stringify({
    id: shape.id ?? null,
    type: shape.type ?? null,
    fillsState,
    strokesState
  });
}

return JSON.stringify({
  id: shape.id ?? null,
  type: shape.type ?? null,
  x: shape.x ?? null,
  y: shape.y ?? null,
  width: shape.width ?? null,
  height: shape.height ?? null,
  fillsState,
  strokesState,
  fills: fillsSummary,
  strokes: strokesSummary,
  text: textContent,
  verbosity: "full"
});