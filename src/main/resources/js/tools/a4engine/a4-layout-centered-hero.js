const title = penpot.createText("{{title}}");
title.name = "Hero/Title";
title.resize(CONTENT_W * 0.85, 120);
title.fontSize = 36;
title.fontWeight = "bold";
title.fills = [{ fillColor: TEXT_COLOR }];
title.x = PAGE_W / 2 - (CONTENT_W * 0.85) / 2;
title.y = flowY;
created.push(title);

const desc = penpot.createText("{{paragraph}}");
desc.name = "Hero/Description";
desc.resize(CONTENT_W * 0.72, 100);
desc.fontSize = 17;
desc.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.72 }];
desc.x = PAGE_W / 2 - (CONTENT_W * 0.72) / 2;
desc.y = title.y + title.height + 20;
created.push(desc);

flowY = desc.y + desc.height + 40;
