const shapes = resolvedShapes.filter(Boolean);
if (shapes.length < 2) throw new Error("Need at least 2 shapes");

const group = penpot.group(shapes);
if (!group) throw new Error("Group creation failed");

if (groupName && groupName.trim() !== "") {
    group.name = groupName;
}

return {
    groupId: group.id
};