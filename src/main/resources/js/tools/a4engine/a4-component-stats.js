flowY += COMPONENT_SPACING;

const stats = [
    { v: "4.9/5",  l: "Note clients" },
    { v: "2 000+", l: "Avis vérifiés" },
    { v: "98 %",   l: "Recommandent" }
];
const colW = CONTENT_W / stats.length;

stats.forEach((s, i) => {
    if (i > 0) {
        const sep = penpot.createRectangle();
        sep.name = "Stats/Separator";
        sep.x = MARGIN + i * colW - 1;
        sep.y = flowY;
        sep.resize(1, 56);
        sep.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.12 }];
        created.push(sep);
    }

    const val = penpot.createText(s.v);
    val.name = "Stats/Value";
    val.x = MARGIN + i * colW;
    val.y = flowY;
    val.fontSize = 28;
    val.fontWeight = "bold";
    val.fills = [{ fillColor: "{{textColor}}" }];
    val.resize(colW - 12, 10);
    created.push(val);

    const lbl = penpot.createText(s.l);
    lbl.name = "Stats/Label";
    lbl.x = MARGIN + i * colW;
    lbl.y = flowY + 34;
    lbl.fontSize = 12;
    lbl.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.6 }];
    lbl.resize(colW - 12, 10);
    created.push(lbl);
});

flowY += 90;
