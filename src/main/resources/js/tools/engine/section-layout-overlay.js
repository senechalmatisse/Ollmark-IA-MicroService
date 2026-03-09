const overlay = penpot.createRectangle();
overlay.x = section.x;
overlay.y = section.y;
overlay.resize(section.width, section.height);
overlay.fills = [{ fillColor: 'rgba(0,0,0,0.45)', fillOpacity: 1 }];
created.push(overlay);

const columnWidth = 720;
columnX = section.x + (section.width - columnWidth) / 2;
flowY = section.y + 180;