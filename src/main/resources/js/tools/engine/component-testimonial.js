flowY += COMPONENT_SPACING;
const card = penpot.createRectangle();
card.name = "Testimonial/Card";
card.x = columnX;
card.y = flowY;
card.resize(COMPONENT_WIDTH, 10);
card.borderRadius = 16;
card.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.07 }];
card.strokes = [{ strokeColor: ACCENT_COLOR, strokeWidth: 1, strokeOpacity: 0.25 }];
created.push(card);

const accentBar = penpot.createRectangle();
accentBar.name = "Testimonial/AccentBar";
accentBar.x = columnX;
accentBar.y = flowY;
accentBar.resize(4, 60);
accentBar.borderRadius = 2;
accentBar.fills = [{ fillColor: ACCENT_COLOR, fillOpacity: 1 }];
created.push(accentBar);

const STAR_SIZE = 10;
const STAR_GAP = 16;
for (let i = 0; i < 5; i++) {
    const star = penpot.createEllipse();
    star.name = "Testimonial/Star";
    star.x = columnX + 20 + i * STAR_GAP;
    star.y = flowY + 20;
    star.resize(STAR_SIZE, STAR_SIZE);
    star.fills = [{ fillColor: ACCENT_COLOR, fillOpacity: 1 }];
    created.push(star);
}

const quote = penpot.createText("\u201cIncroyable qualité, livraison ultra rapide. Je recommande sans hésiter.\u201d");
quote.name = "Testimonial/Quote";
quote.x = columnX + 20;
quote.y = flowY + 44;
quote.fontSize = 15;
quote.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.92 }];
quote.resize(COMPONENT_WIDTH - 44, 10);
created.push(quote);

const divider = penpot.createRectangle();
divider.name = "Testimonial/Divider";
divider.x = columnX + 20;
divider.y = quote.y + quote.height + 14;
divider.resize(36, 2);
divider.borderRadius = 1;
divider.fills = [{ fillColor: ACCENT_COLOR, fillOpacity: 0.5 }];
created.push(divider);

const author = penpot.createText("— Client vérifié · Note : 5/5");
author.name = "Testimonial/Author";
author.x = columnX + 20;
author.y = quote.y + quote.height + 22;
author.fontSize = 13;
author.fontWeight = "bold";
author.fills = [{ fillColor: TEXT_COLOR, fillOpacity: 0.72 }];
author.resize(COMPONENT_WIDTH - 44, 10);
created.push(author);

const totalHeight = author.y + author.height - card.y + 24;
card.resize(COMPONENT_WIDTH, totalHeight);
accentBar.resize(4, totalHeight);
flowY += totalHeight + 28;
