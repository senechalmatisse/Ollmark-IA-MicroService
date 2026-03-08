const nameTxt = penpot.createText("{{name}}");
nameTxt.x = columnX + symbolSize + 24;
nameTxt.y = columnY + (symbolSize / 2) + 10;
nameTxt.fontSize = 32;
nameTxt.fontWeight = "{{weight}}";
nameTxt.fills = [{ fillColor: '{{color}}' }];
created.push(nameTxt);

if ("{{tagline}}" !== "") {
    const tagTxt = penpot.createText("{{tagline}}");
    tagTxt.x = nameTxt.x;
    tagTxt.y = nameTxt.y + 25;
    tagTxt.fontSize = 14;
    tagTxt.fills = [{ fillColor: '{{color}}', fillOpacity: 0.6 }];
    created.push(tagTxt);
}