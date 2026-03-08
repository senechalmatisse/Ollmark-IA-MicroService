const decorativeLine = penpot.createRectangle();
decorativeLine.x = section.x + section.width / 2 - 50;
decorativeLine.y = section.y + 120;
decorativeLine.resize(100, 2);
decorativeLine.fills = [{ fillColor: '#FFFFFF', fillOpacity: 0.6 }];
created.push(decorativeLine);

const columnWidth = 600;
columnX = section.x + (section.width - columnWidth) / 2;
flowY = section.y + 180;