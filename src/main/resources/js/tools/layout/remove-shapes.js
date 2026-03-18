const root = page.root;
if (!root) throw new Error("No page root");

if (typeof root.appendChild !== "function" && typeof root.insertChild !== "function") {
    throw new Error("Page root is not a container");
}

const shapes = resolvedShapes.filter(Boolean);
if (shapes.length === 0) throw new Error("No shapes found");

const movedIds = [];

for (const shape of shapes) {
    if (!shape || !shape.id) continue;

  // Si déjà à la racine, on ne fait rien mais on le compte comme traité
    if (shape.parent === root) {
        movedIds.push(shape.id);
        continue;
    }

    if (typeof root.appendChild === "function") {
        root.appendChild(shape);
    } else {
        const index = Array.isArray(root.children) ? root.children.length : 0;
        root.insertChild(index, shape);
    }

    movedIds.push(shape.id);
}

return {
    ids: movedIds
};