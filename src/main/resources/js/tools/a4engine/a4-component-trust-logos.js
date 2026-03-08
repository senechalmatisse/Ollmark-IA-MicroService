flowY += COMPONENT_SPACING;

const logoLabel = penpot.createText("ILS NOUS FONT CONFIANCE :");
logoLabel.x = MARGIN;
logoLabel.y = flowY;
logoLabel.fontSize = 10;
logoLabel.fontWeight = "bold";
logoLabel.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.6 }];
created.push(logoLabel);

flowY += 30;

const logoGap = (CONTENT_W - (4 * 40)) / 3;
for (let i = 0; i < 4; i++) {
    const l = penpot.createEllipse();
    l.resize(40, 40);
    l.x = MARGIN + (i * (40 + logoGap));
    l.y = flowY;
    l.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.2 }];
    created.push(l);
}

flowY += 80;