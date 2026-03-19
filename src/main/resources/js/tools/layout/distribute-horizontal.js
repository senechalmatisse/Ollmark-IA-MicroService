const shapes = resolvedShapes.filter(Boolean);
if (shapes.length < 3) throw new Error("Need at least 3 shapes");

penpot.distributeHorizontal(shapes);

return {
    ids: shapes.map(shape => shape.id)
};