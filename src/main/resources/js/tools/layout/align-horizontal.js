const shapes = resolvedShapes.filter(Boolean);
if (shapes.length < 2) throw new Error("Need at least 2 shapes");

if (!["left", "center", "right"].includes(alignment)) {
    throw new Error("Invalid horizontal alignment: " + alignment);
}

penpot.alignHorizontal(shapes, alignment);

return {
    ids: shapes.map(shape => shape.id)
};