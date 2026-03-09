const embText = penpot.createText("{{name}}");
embText.x = columnX + (symbolSize / 2);
embText.y = columnY + (symbolSize / 2) + 10;
embText.fontSize = 18;
embText.fontWeight = "bold";
embText.fills = [{ fillColor: '#FFFFFF' }];
embText.textAlignHorizontal = "center";
created.push(embText);