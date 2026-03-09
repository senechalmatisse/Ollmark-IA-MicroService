const HERO_TEXT_W = CONTENT_W * 0.55;
const leftX = MARGIN;
const rightX = MARGIN + HERO_TEXT_W + 20;

const title = penpot.createText("{{title}}");
title.x = leftX;
title.y = flowY;
title.fontSize = 36;
title.fontWeight = "bold";
title.fills = [{ fillColor: TEXT_COLOR }];
title.resize(HERO_TEXT_W, 120);
created.push(title);

const desc = penpot.createText("{{paragraph}}");
desc.x = leftX;
desc.y = flowY + 120;
desc.fontSize = 16;
desc.resize(HERO_TEXT_W, 100);
desc.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.75 }];
created.push(desc);

const img = penpot.createRectangle();
img.resize(CONTENT_W - HERO_TEXT_W - 20, 260);
img.x = rightX;
img.y = flowY;
img.borderRadius = 18;
img.fills = [{ fillColor: "#E5E7EB" }];
created.push(img);

flowY += 280;