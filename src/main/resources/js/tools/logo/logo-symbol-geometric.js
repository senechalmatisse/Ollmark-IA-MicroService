const base = penpot.createRectangle();
base.resize(symbolSize, symbolSize);
base.x = columnX;
base.y = columnY;
base.borderRadius = {{borderRadius}};
base.fills = [{{fillStyle}}];
base.rotation = 45;
created.push(base);

const accent = penpot.createEllipse();
accent.resize(symbolSize * 0.4, symbolSize * 0.4);
accent.x = columnX + (symbolSize * 0.1);
accent.y = columnY + (symbolSize * 0.3);
accent.fills = [{ fillColor: '{{secondaryColor}}', fillOpacity: 0.4 }];
created.push(accent);