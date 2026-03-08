const nameV = penpot.createText("{{name}}");
nameV.x = columnX + (symbolSize / 2);
nameV.y = columnY + symbolSize + 40;
nameV.fontSize = 28;
nameV.fontWeight = "{{weight}}";
nameV.fills = [{ fillColor: '{{color}}' }];
nameV.textAlignHorizontal = "center";
created.push(nameV);

if ("{{tagline}}" !== "") {
    const tagV = penpot.createText("{{tagline}}");
    tagV.x = nameV.x;
    tagV.y = nameV.y + 30;
    tagV.fontSize = 12;
    tagV.fills = [{ fillColor: '{{color}}', fillOpacity: 0.5 }];
    tagV.textAlignHorizontal = "center";
    created.push(tagV);
}