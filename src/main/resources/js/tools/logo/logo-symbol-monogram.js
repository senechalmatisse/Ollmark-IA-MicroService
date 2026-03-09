const frame = penpot.createRectangle();
frame.resize(symbolSize, symbolSize);
frame.x = columnX;
frame.y = columnY;
frame.borderRadius = {{borderRadius}};
frame.fills = [{{fillStyle}}];
created.push(frame);

const char = penpot.createText("{{letter}}");
char.x = columnX + (symbolSize * 0.2);
char.y = columnY + (symbolSize * 0.75);
char.fontSize = symbolSize * 0.7;
char.fontWeight = "bold";
char.fills = [{ fillColor: '{{textColor}}' }];
created.push(char);