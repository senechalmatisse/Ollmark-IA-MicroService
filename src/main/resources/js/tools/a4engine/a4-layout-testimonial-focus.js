const card = penpot.createRectangle();
card.resize(CONTENT_W, 120);
card.x = MARGIN;
card.y = flowY;
card.borderRadius = 16;
card.fills = [{ fillColor: "#F1F5F9" }];
created.push(card);

const quote = penpot.createText("\u201cProduit incroyable. Je recommande.\u201d");
quote.x = MARGIN + 40;
quote.y = flowY + 45;
quote.fontSize = 18;
quote.fontWeight = "bold";
quote.fills = [{ fillColor: TEXT_COLOR }];
created.push(quote);

flowY += 160;