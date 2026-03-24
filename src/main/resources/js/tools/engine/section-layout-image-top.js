const preview = penpot.createRectangle();
preview.x = section.x + 200;
preview.y = section.y + 40;
preview.resize(section.width * 0.6, 260);
preview.borderRadius = 20;
preview.fills = [{ fillColor: "#FFFFFF", fillOpacity: 0.12 }];
created.push(preview);

columnX = section.x + 200;
flowY = section.y + 340;