const shape1 = penpot.createEllipse();
shape1.resize(symbolSize * 0.8, symbolSize * 0.8);
shape1.x = columnX;
shape1.y = columnY;
shape1.fills = [{{fillStyle}}];
created.push(shape1);

const shape2 = penpot.createEllipse();
shape2.resize(symbolSize * 0.8, symbolSize * 0.8);
shape2.x = columnX + (symbolSize * 0.3);
shape2.y = columnY + (symbolSize * 0.1);
shape2.fills = [{ fillColor: '{{secondaryColor}}', fillOpacity: 0.7 }];
created.push(shape2);