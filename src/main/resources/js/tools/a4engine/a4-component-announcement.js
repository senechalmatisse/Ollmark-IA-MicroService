flowY += 10;

const annBar = penpot.createRectangle();
annBar.x = MARGIN;
annBar.y = flowY;
annBar.resize(CONTENT_W, 42);
annBar.borderRadius = 14;
annBar.fills = [{ fillColor: "#0F172A" }];
created.push(annBar);

const annTxt = penpot.createText("OFFRE EXCLUSIVE — DISPONIBLE AUJOURD'HUI");
annTxt.fontSize = 12;
annTxt.fontWeight = "bold";
annTxt.fills = [{ fillColor: "{{textColor}}" }];
annTxt.x = PAGE_W / 2 - 120;
annTxt.y = flowY + 13;
created.push(annTxt);

flowY += 80;