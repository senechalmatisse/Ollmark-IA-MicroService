flowY += COMPONENT_SPACING;

const features = [
    "Accès illimité",
    "Support prioritaire",
    "Mises à jour incluses"
];

features.forEach((f, i) => {
    const col = CONTENT_W / 3;
    const x = MARGIN + col * i;

    const dot = penpot.createEllipse();
    dot.resize(10, 10);
    dot.x = x;
    dot.y = flowY + 6;
    dot.fills = [{ fillColor: "{{textColor}}" }];
    created.push(dot);

    const txt = penpot.createText(f);
    txt.x = x + 18;
    txt.y = flowY;
    txt.fontSize = 15;
    txt.fills = [{ fillColor: "{{textColor}}" }];
    created.push(txt);
});

flowY += 90;