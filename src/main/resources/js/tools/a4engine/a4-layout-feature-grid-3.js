flowY += 40;
const colW = CONTENT_W / 3;

for (let i = 0; i < 3; i++) {
    const card = penpot.createRectangle();
    card.resize(colW - 20, 120);
    card.x = MARGIN + i * colW;
    card.y = flowY;
    card.borderRadius = 14;
    card.fills = [{ fillColor: "#F1F5F9" }];
    created.push(card);

    const txt = penpot.createText("Feature");
    txt.x = card.x + 20;
    txt.y = card.y + 40;
    txt.fontSize   = 16;
    txt.fontWeight = "bold";
    txt.fills = [{ fillColor: TEXT_COLOR }];
    created.push(txt);
}

flowY += 160;