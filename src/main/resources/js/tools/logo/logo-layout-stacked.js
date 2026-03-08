const mainS = penpot.createText("{{name}}");
mainS.x = columnX;
mainS.y = columnY + symbolSize + 30;
mainS.fontSize = 36;
mainS.fontWeight = "{{weight}}";
mainS.fills = [{ fillColor: '{{color}}' }];
created.push(mainS);

const subS = penpot.createText("{{tagline}}");
subS.x = columnX;
subS.y = mainS.y + 45;
subS.fontSize = 16;
subS.fontWeight = "bold";
subS.fills = [{ fillColor: '{{color}}' }];
created.push(subS);