const created = [];
const PAGE_W = 595;
const PAGE_H = 842;
const MARGIN = 60;
const CONTENT_W = PAGE_W - MARGIN * 2;
let flowY = 80;

const bg = penpot.createRectangle();
bg.resize(PAGE_W, PAGE_H);
bg.x = {{posX}};
bg.y = {{posY}};
created.push(bg);