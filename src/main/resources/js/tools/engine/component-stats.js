flowY += COMPONENT_SPACING;
const stats = [
    { v: "10K+", l: "clients" },
    { v: "4.9/5", l: "avis" },
    { v: "24h",   l: "livraison" }
];
const statGap = COMPONENT_WIDTH / stats.length;
stats.forEach((s, i) => {
    const v = penpot.createText(s.v);
    v.x = columnX + i * statGap;
    v.y = flowY;
    v.fontSize = 26;
    v.fontWeight = "bold";
    v.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 1 }];
    v.resize(120, 10);
    created.push(v);
    const l = penpot.createText(s.l);
    l.x = v.x;
    l.y = flowY + v.height + 4;
    l.fontSize = 14;
    l.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.8 }];
    l.resize(120, 10);
    created.push(l);
});
flowY += 80;