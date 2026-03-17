const board = page.getShapeById(boardId);
if (!board) throw new Error("Board not found");

if (typeof board.appendChild !== "function") {
    throw new Error("Target shape is not a board/container");
}

const shapes = resolvedShapes.filter(Boolean);
if (shapes.length === 0) throw new Error("No shapes found");

shapes.forEach(shape => board.appendChild(shape));

return {
    ids: shapes.map(shape => shape.id)
};