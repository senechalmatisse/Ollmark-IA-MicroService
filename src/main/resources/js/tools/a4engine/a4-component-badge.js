flowY += 10;

const b = penpot.createRectangle();
b.resize(120, 28);
b.x = MARGIN;
b.y = flowY;
b.borderRadius = 99;
b.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.25 }];
created.push(b);

const bTxt = penpot.createText("TOP PRODUIT");
bTxt.x = b.x + 15;
bTxt.y = b.y + 7;
bTxt.fontSize = 11;
bTxt.fontWeight = "bold";
bTxt.fills = [{ fillColor: "{{textColor}}" }];
created.push(bTxt);

flowY += 50;