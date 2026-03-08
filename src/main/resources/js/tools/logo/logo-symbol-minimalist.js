const track = penpot.createRectangle();
track.resize(symbolSize, 6);
track.x = columnX;
track.y = columnY + (symbolSize / 2);
track.borderRadius = 99;
track.fills = [{ fillColor: '{{secondaryColor}}', fillOpacity: 0.2 }];
created.push(track);

const bullet = penpot.createEllipse();
bullet.resize(24, 24);
bullet.x = columnX + (symbolSize * 0.4);
bullet.y = columnY + (symbolSize / 2) - 9;
bullet.fills = [{{fillStyle}}];
created.push(bullet);