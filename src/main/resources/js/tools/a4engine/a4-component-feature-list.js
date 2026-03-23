flowY += COMPONENT_SPACING;

const features = [
    "Accès illimité",
    "Support prioritaire 24/7",
    "Mises à jour incluses"
];

features.forEach((f, i) => {
    const col = CONTENT_W / 3;
    const x = MARGIN + col * i;

    const dot = penpot.createRectangle();
    dot.name = "Feature/Bullet";
    dot.resize(12, 12);
    dot.x = x;
    dot.y = flowY + 5;
    dot.borderRadius = 6;
    dot.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.85 }];
    created.push(dot);

    const txt = penpot.createText(f);
    txt.name = "Feature/Text";
    txt.x = x + 20;
    txt.y = flowY;
    txt.fontSize = 15;
    txt.fontWeight = "bold";
    txt.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.9 }];
    txt.resize(col - 28, 10);
    created.push(txt);
});

flowY += 60;
