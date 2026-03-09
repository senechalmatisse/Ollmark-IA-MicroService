const container = penpot.createRectangle();
container.resize(symbolSize, symbolSize);
container.x = columnX;
container.y = columnY;
container.borderRadius = {{borderRadius}};
container.fills = [{{fillStyle}}];
created.push(container);

const strip = penpot.createRectangle();
strip.resize(symbolSize, symbolSize * 0.25);
strip.x = columnX;
strip.y = columnY + (symbolSize * 0.6);
strip.fills = [{ fillColor: '{{textColor}}', fillOpacity: 0.2 }];
created.push(strip);