const IMAGE_W = CONTENT_W * 0.45;
const TEXT_W  = CONTENT_W - IMAGE_W - 20;
const imgX = MARGIN;
const textX = MARGIN + IMAGE_W + 20;

const img = penpot.createRectangle();
img.resize(IMAGE_W, 260);
img.x = imgX;
img.y = flowY;
img.borderRadius = 18;
img.fills = [{ fillColor: "#E5E7EB" }];
created.push(img);

const title = penpot.createText("{{title}}");
title.x = textX;
title.y = flowY;
title.fontSize   = 34;
title.fontWeight = "bold";
title.fills = [{ fillColor: TEXT_COLOR }];
title.resize(TEXT_W, 200);
created.push(title);

const desc = penpot.createText("{{paragraph}}");
desc.x = textX;
desc.y = flowY + 120;
desc.fontSize = 16;
desc.resize(TEXT_W, 100);
desc.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.75 }];
created.push(desc);

flowY += 280;