const minW = {{minW}};
const minH = {{minH}};
const padX = {{padX}};

const posX = {{posX}};
const posY = {{posY}};

const bg = '{{bg}}';
const textColor = '{{textColor}}';
const radius = {{radius}};
const label = '{{label}}';

const estimatedTextWidth = label.length * 9;
const finalWidth = Math.max(minW, estimatedTextWidth + padX * 2);

const rect = penpot.createRectangle();
rect.x = posX;
rect.y = posY;
rect.resize(finalWidth, minH);
rect.fills = [{ fillColor: bg, fillOpacity: 1 }];
rect.borderRadius = radius;
rect.name = "Button";

const text = penpot.createText(label);
text.fontSize = 18;
text.fontWeight = "bold";
text.fills = [{ fillColor: textColor, fillOpacity: 1 }];

text.x = rect.x + (finalWidth - estimatedTextWidth) / 2;
text.y = rect.y + 35;
return rect.id;