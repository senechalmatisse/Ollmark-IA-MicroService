const columnWidth = 600;
columnX = section.x + (section.width - columnWidth) / 2;
flowY = section.y + 80;

// Top decorative accent line
const topLine = penpot.createRectangle();
topLine.name = "Luxury/TopLine";
topLine.x = columnX + columnWidth / 2 - 40;
topLine.y = flowY;
topLine.resize(80, 2);
topLine.borderRadius = 1;
topLine.fills = [{ fillColor: '#FFFFFF', fillOpacity: 0.55 }];
created.push(topLine);

// Small diamond accent
const diamond = penpot.createRectangle();
diamond.name = "Luxury/Diamond";
diamond.x = columnX + columnWidth / 2 - 5;
diamond.y = flowY + 8;
diamond.resize(10, 10);
diamond.borderRadius = 1;
diamond.fills = [{ fillColor: '#FFFFFF', fillOpacity: 0.4 }];
created.push(diamond);

// Side decorative lines
const leftLine = penpot.createRectangle();
leftLine.name = "Luxury/LeftLine";
leftLine.x = columnX;
leftLine.y = section.y + 40;
leftLine.resize(1, section.height - 80);
leftLine.fills = [{ fillColor: '#FFFFFF', fillOpacity: 0.12 }];
created.push(leftLine);

const rightLine = penpot.createRectangle();
rightLine.name = "Luxury/RightLine";
rightLine.x = columnX + columnWidth;
rightLine.y = section.y + 40;
rightLine.resize(1, section.height - 80);
rightLine.fills = [{ fillColor: '#FFFFFF', fillOpacity: 0.12 }];
created.push(rightLine);

flowY = section.y + 140;
