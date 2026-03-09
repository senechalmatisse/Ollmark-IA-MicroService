const selShapes = penpot.selection;
const shape = (selShapes && selShapes.length) ? selShapes[0] : null;
if (!shape) throw new Error('No shape to operate on. Select a shape in Penpot.');