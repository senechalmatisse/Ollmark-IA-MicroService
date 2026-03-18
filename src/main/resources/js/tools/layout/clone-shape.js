if (!shape) throw new Error("Shape not found");

const clone = shape.clone();
if (!clone) throw new Error("Clone failed");

clone.x = shape.x + offsetX;
clone.y = shape.y + offsetY;

return {
    cloneId: clone.id
};