const product = penpot.createRectangle();
product.resize(CONTENT_W * 0.45, 320);
product.x = MARGIN;
product.y = flowY;
product.borderRadius = 22;
product.fills = [{ fillColor: "#E5E7EB" }];
created.push(product);

const title = penpot.createText("{{title}}");
title.x = MARGIN + CONTENT_W * 0.5;
title.y = flowY + 60;
title.fontSize = 38;
title.fontWeight = "bold";
title.fills = [{ fillColor: TEXT_COLOR }];
title.resize(CONTENT_W * 0.45, 160);
created.push(title);

flowY += 340;