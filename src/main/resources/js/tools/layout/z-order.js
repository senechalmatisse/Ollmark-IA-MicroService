const shapes = resolvedShapes.filter(Boolean);
if (shapes.length === 0) throw new Error("No shapes found");

if (
    !["bringToFront", "bringForward", "sendToBack", "sendBackward"].includes(action)
) {
    throw new Error("Invalid z-order action: " + action);
}

shapes.forEach(shape => {
    if (typeof shape[action] !== "function") {
        throw new Error("Z-order method not available: " + action);
    }
    shape[action]();
});

return {
    ids: shapes.map(shape => shape.id)
};