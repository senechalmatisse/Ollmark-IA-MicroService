flowY += COMPONENT_SPACING;

const statVal = penpot.createText("4.9 / 5");
statVal.x = PAGE_W - MARGIN - 120;
statVal.y = flowY;
statVal.fontSize = 36;
statVal.fontWeight = "bold";
statVal.fills = [{ fillColor: "{{textColor}}" }];
created.push(statVal);

const statSub = penpot.createText("Basé sur 2000+ avis clients");
statSub.x = statVal.x;
statSub.y = flowY + 40;
statSub.fontSize = 14;
statSub.fills = [{ fillColor: "{{textColor}}" }];
created.push(statSub);

flowY += 100;