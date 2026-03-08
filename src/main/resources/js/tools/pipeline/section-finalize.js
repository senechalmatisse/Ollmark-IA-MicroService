const group = penpot.group(created);
if (!group) throw new Error("Grouping failed");
group.name = "HeroSection";
return {
    success: true,
    sectionId: group.id
};