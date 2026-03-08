flowY += 40;

const price = penpot.createRectangle();
price.resize(260, 160);
price.x = PAGE_W / 2 - 130;
price.y = flowY;
price.borderRadius = 20;
price.fills = [{ fillColor: "#FFFFFF" }];
created.push(price);

const priceTxt = penpot.createText("99\u20AC / mois");
priceTxt.x = price.x + 55;
priceTxt.y = flowY + 60;
priceTxt.fontSize = 30;
priceTxt.fontWeight = "bold";
priceTxt.fills = [{ fillColor: TEXT_COLOR }];
created.push(priceTxt);

flowY += 200;