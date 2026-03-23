flowY += COMPONENT_SPACING;
const features = [
    "Qualité premium garantie",
    "Livraison en 24h offerte",
    "Satisfait ou remboursé 30 jours"
];
features.forEach(f => {
    const dot = penpot.createRectangle();
    dot.name = "Feature/Bullet";
    dot.x = columnX;
    dot.y = flowY + 7;
    dot.resize(10, 10);
    dot.borderRadius = 5;
    dot.fills = [{ fillColor: ACCENT_COLOR, fillOpacity: 1 }];
    created.push(dot);

    const txt = penpot.createText(f);
    txt.name = "Feature/Text";
    txt.x = columnX + 22;
    txt.y = flowY;
    txt.fontSize = 16;
    txt.fontWeight = "bold";
    txt.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.92 }];
    txt.resize(COMPONENT_WIDTH - 22, 10);
    created.push(txt);

    flowY += txt.height + 20;
});
flowY += 12;
