const ribbon = penpot.createRectangle();
ribbon.resize(110, 34);
ribbon.x = PAGE_W - 140;
ribbon.y = 30;
ribbon.borderRadius = 6;
ribbon.fills = [{ fillColor: "{{accentColor}}" }];
created.push(ribbon);

const ribTxt = penpot.createText("-{{discountValue}}%");
ribTxt.x = ribbon.x + 30;
ribTxt.y = ribbon.y + 8;
ribTxt.fontSize = 16;
ribTxt.fontWeight = "bold";
ribTxt.fills = [{ fillColor: "{{textColor}}" }];
created.push(ribTxt);