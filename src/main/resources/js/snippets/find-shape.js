let shape = null;
try {
    shape = penpot.currentPage.getShapeById('{{shapeId}}');
} catch (e) {
    if (penpot.selection.length > 0) shape = penpot.selection[0];
}
if (!shape) throw new Error('Shape not found. ID: {{shapeId}}. Please select a shape in Penpot.');