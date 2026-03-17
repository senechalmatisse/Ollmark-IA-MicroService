const allowedActions = [
  "bringToFront",
  "bringForward",
  "sendToBack",
  "sendBackward"
];

if (!allowedActions.includes(action)) {
  throw new Error("Invalid z-order action: " + action);
}

const shapes = resolvedShapes.filter(Boolean);
if (shapes.length === 0) {
  throw new Error("No shapes found");
}

function getChildren(parent) {
  if (!parent) return null;
  if (Array.isArray(parent.children)) return parent.children.filter(Boolean);
  return null;
}

function computeTargetIndex(currentAction, beforeIndex, lastIndex) {
  switch (currentAction) {
    case "bringForward":
      return Math.min(beforeIndex + 1, lastIndex);
    case "bringToFront":
      return lastIndex;
    case "sendBackward":
      return Math.max(beforeIndex - 1, 0);
    case "sendToBack":
      return 0;
    default:
      throw new Error("Unsupported z-order action: " + currentAction);
  }
}

const results = [];

for (const shape of shapes) {
  if (!shape || !shape.id) continue;

  const parent = shape.parent;
  if (!parent) {
    throw new Error(`Shape ${shape.id} has no parent`);
  }

  const childrenBefore = getChildren(parent);
  if (!childrenBefore || childrenBefore.length === 0) {
    throw new Error(`Unable to inspect children for parent of shape ${shape.id}`);
  }

  const beforeIndex = shape.parentIndex;
  if (typeof beforeIndex !== "number" || beforeIndex < 0) {
    throw new Error(`Invalid parentIndex before move for shape ${shape.id}: ${beforeIndex}`);
  }

  const lastIndex = childrenBefore.length - 1;
  const targetIndex = computeTargetIndex(action, beforeIndex, lastIndex);

  if (targetIndex === beforeIndex) {
    results.push({
      id: shape.id,
      action,
      beforeIndex,
      targetIndex,
      afterIndex: beforeIndex,
      changed: false,
      reason: "already-at-boundary"
    });
    continue;
  }

  shape.setParentIndex(targetIndex);

  const afterIndex = shape.parentIndex;
  const childrenAfter = getChildren(parent);

  const changed = afterIndex !== beforeIndex;
  const reachedTarget = afterIndex === targetIndex;

  if (!changed || !reachedTarget) {
    throw new Error(
      `Z-order action "${action}" failed for shape ${shape.id} ` +
      `(beforeIndex=${beforeIndex}, targetIndex=${targetIndex}, afterIndex=${afterIndex})`
    );
  }

  results.push({
    id: shape.id,
    action,
    beforeIndex,
    targetIndex,
    afterIndex,
    changed,
    beforeOrder: childrenBefore.map(s => s.id),
    afterOrder: Array.isArray(childrenAfter) ? childrenAfter.map(s => s.id) : null
  });
}

return {
  success: true,
  action,
  count: results.length,
  results
};