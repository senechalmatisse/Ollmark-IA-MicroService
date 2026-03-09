const product = penpot.createRectangle();
product.x = section.x + ({{left}} ? 64 : section.width * 0.55);
product.y = section.y + 64;
product.resize(section.width * 0.4, section.height - 128);
product.borderRadius = 28;
product.fills = [{ fillColor: 'rgba(255,255,255,0.15)', fillOpacity: 1 }];
created.push(product);

columnX = section.x + ({{left}} ? section.width * 0.5 : 64);
flowY = section.y + 64;