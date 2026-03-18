const groups = resolvedShapes.filter(shape => shape && shape.type === "group");
if (groups.length === 0) throw new Error("No groups found");

penpot.ungroup(groups[0], ...groups.slice(1));

return {
    ids: groups.map(group => group.id)
};