const group = penpot.group(created);
if (!group) throw new Error("Grouping failed");
group.name = "A4 Marketing Section";
return {
    success: true,
    sectionId: group.id
};