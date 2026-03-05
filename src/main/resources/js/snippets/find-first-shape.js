let shape = null;
try { shape = penpot.currentPage.getShapeById('{{shapeId}}'); } catch (e) {}
if (!shape) {
    const sel = penpot.selection;
    shape = (sel && sel.length) ? sel[0] : null;
}
if (!shape) throw new Error('No shape found. ID: {{shapeId}}. Please select a shape.');