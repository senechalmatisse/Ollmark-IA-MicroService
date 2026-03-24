flowY += COMPONENT_SPACING;
const stats = [
    { v: "10K+",  l: "clients" },
    { v: "4.9/5", l: "satisfaction" },
    { v: "24h",   l: "livraison" }
];
const statSep = COMPONENT_WIDTH / stats.length;
stats.forEach((s, i) => {
    if (i > 0) {
        const sep = penpot.createRectangle();
        sep.name = "Stats/Separator";
        sep.x = columnX + i * statSep - 16;
        sep.y = flowY;
        sep.resize(1, 64);
        sep.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.12 }];
        created.push(sep);
    }
    const v = penpot.createText(s.v);
    v.name = "Stats/Value";
    v.x = columnX + i * statSep;
    v.y = flowY;
    v.fontSize = 32;
    v.fontWeight = "bold";
    v.fills = [{ fillColor: ACCENT_COLOR, fillOpacity: 1 }];
    v.resize(statSep - 24, 10);
    created.push(v);
    const l = penpot.createText(s.l);
    l.name = "Stats/Label";
    l.x = v.x;
    l.y = flowY + v.height + 6;
    l.fontSize = 13;
    l.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.65 }];
    l.resize(statSep - 24, 10);
    created.push(l);
});
flowY += 90;
