const shapes = resolvedShapes.filter(Boolean);
if (shapes.length < 2) throw new Error("Need at least 2 shapes");

if (!["top", "center", "bottom"].includes(alignment)) {
    throw new Error("Invalid vertical alignment: " + alignment);
}

penpot.alignVertical(shapes, alignment);

return {
    ids: shapes.map(shape => shape.id)
};