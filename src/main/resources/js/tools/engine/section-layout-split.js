const halfWidth = section.width / 2;
const preview = penpot.createRectangle();

preview.x = section.x + ({{imageLeft}} ? 64 : halfWidth);
preview.y = section.y + 64;
preview.resize(halfWidth - 64, section.height - 128);
preview.borderRadius = 24;
preview.fills = [{ fillColor: "#FFFFFF", fillOpacity: 0.12 }];
created.push(preview);

columnX = section.x + ({{imageLeft}} ? halfWidth + 64 : 64);
flowY   = section.y + 64;