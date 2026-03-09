const title = penpot.createText("{{title}}");
title.resize(CONTENT_W * 0.8, 120);
title.fontSize = 34;
title.fontWeight = "bold";
title.fills = [{ fillColor: TEXT_COLOR }];
title.x = PAGE_W / 2 - (CONTENT_W * 0.8) / 2;
title.y = flowY;
created.push(title);

const desc = penpot.createText("{{paragraph}}");
desc.resize(CONTENT_W * 0.7, 100);
desc.fontSize = 17;
desc.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.75 }];
desc.x = PAGE_W / 2 - (CONTENT_W * 0.7) / 2;
desc.y = flowY + 90;
created.push(desc);

flowY += 200;