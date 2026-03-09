flowY += COMPONENT_SPACING;
const features = [
    "Qualité premium",
    "Livraison en 24h",
    "Satisfait ou remboursé"
];
features.forEach(f => {
    const dot = penpot.createEllipse();
    dot.x = columnX;
    dot.y = flowY + 6;
    dot.resize(8, 8);
    dot.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 1 }];
    created.push(dot);
    const txt = penpot.createText(f);
    txt.x = columnX + 20;
    txt.y = flowY;
    txt.fontSize = 16;
    txt.fontWeight = "bold";
    txt.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.9 }];
    txt.resize(COMPONENT_WIDTH - 20, 10);
    created.push(txt);
    flowY += txt.height + 18;
});
flowY += 10;