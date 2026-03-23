flowY += COMPONENT_SPACING;

const priceCard = penpot.createRectangle();
priceCard.resize(240, 120);
priceCard.x = PAGE_W / 2 - 120;
priceCard.y = flowY;
priceCard.borderRadius = 20;
priceCard.fills = [{ fillColor: "#FFFFFF", fillOpacity: 0.08 }];
priceCard.strokes = [{ strokeColor: "{{textColor}}", strokeWidth: 2 }];
created.push(priceCard);

const pText = penpot.createText("À partir de {{pricingLabel}}");
pText.x = priceCard.x + 55;
pText.y = priceCard.y + 40;
pText.fontSize = 26;
pText.fontWeight = "bold";
pText.fills = [{ fillColor: "{{textColor}}" }];
created.push(pText);

flowY += 150;