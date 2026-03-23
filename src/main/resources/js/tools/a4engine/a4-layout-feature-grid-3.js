flowY += 40;
const colW = CONTENT_W / 3;
const feats = ["{{feature0}}", "{{feature1}}", "{{feature2}}"];
const nums  = ["01", "02", "03"];

for (let i = 0; i < 3; i++) {
    const card = penpot.createRectangle();
    card.name = "FeatureGrid/Card";
    card.resize(colW - 24, 148);
    card.x = MARGIN + i * colW;
    card.y = flowY;
    card.borderRadius = 16;
    card.fills = [{ fillColor: "#F8FAFC" }];
    card.strokes = [{ strokeColor: "#E2E8F0", strokeWidth: 1 }];
    created.push(card);

    const num = penpot.createText(nums[i]);
    num.name = "FeatureGrid/Number";
    num.x = card.x + 20;
    num.y = card.y + 20;
    num.fontSize = 13;
    num.fontWeight = "bold";
    num.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.3 }];
    created.push(num);

    const bar = penpot.createRectangle();
    bar.name = "FeatureGrid/AccentBar";
    bar.x = card.x + 20;
    bar.y = card.y + 46;
    bar.resize(28, 3);
    bar.borderRadius = 2;
    bar.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.45 }];
    created.push(bar);

    const txt = penpot.createText(feats[i]);
    txt.name = "FeatureGrid/Label";
    txt.x = card.x + 20;
    txt.y = card.y + 62;
    txt.fontSize = 16;
    txt.fontWeight = "bold";
    txt.fills = [{ fillColor: TEXT_COLOR }];
    txt.resize(colW - 48, 10);
    created.push(txt);
}

flowY += 190;
