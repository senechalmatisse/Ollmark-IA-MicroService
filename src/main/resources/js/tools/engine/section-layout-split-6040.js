const imageWidth = section.width * 0.6;
const preview = penpot.createRectangle();

preview.x = section.x + ({{imageLeft}} ? 0 : section.width * 0.4);
preview.y = section.y;
preview.resize(imageWidth, section.height);
preview.fills = [{ fillColor: 'rgba(255,255,255,0.10)', fillOpacity: 1 }];
created.push(preview);

columnX = section.x + ({{imageLeft}} ? imageWidth + 64 : 64);
flowY = section.y + 64;