flowY += COMPONENT_SPACING;

const cdBox = penpot.createRectangle();
cdBox.resize(CONTENT_W, 60);
cdBox.x = MARGIN;
cdBox.y = flowY;
cdBox.borderRadius = 12;
cdBox.fills = [{ fillColor: "#F1F5F9" }];
created.push(cdBox);

const cd = penpot.createText("OFFRE FINIT DANS : 02:15:30");
cd.x = MARGIN + 20;
cd.y = flowY + 20;
cd.fontSize = 18;
cd.fontWeight = "bold";
cd.fills = [{ fillColor: "{{textColor}}" }];
created.push(cd);

flowY += 80;