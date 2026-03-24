flowY += 24;

const card = penpot.createRectangle();
card.name = "Testimonial/Card";
card.resize(CONTENT_W, 10);
card.x = MARGIN;
card.y = flowY;
card.borderRadius = 12;
card.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.07 }];
card.strokes = [{ strokeColor: "{{textColor}}", strokeWidth: 1, strokeOpacity: 0.1 }];
created.push(card);

const accentBar = penpot.createRectangle();
accentBar.name = "Testimonial/AccentBar";
accentBar.x = MARGIN;
accentBar.y = flowY;
accentBar.resize(4, 80);
accentBar.borderRadius = 2;
accentBar.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.5 }];
created.push(accentBar);

for (let i = 0; i < 5; i++) {
    const star = penpot.createEllipse();
    star.name = "Testimonial/Star";
    star.x = MARGIN + 20 + i * 16;
    star.y = flowY + 14;
    star.resize(10, 10);
    star.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.9 }];
    created.push(star);
}

const quote = penpot.createText("\u201cUne expérience incroyable, je recommande ce produit à tous mes proches.\u201d");
quote.name = "Testimonial/Quote";
quote.x = MARGIN + 20;
quote.y = flowY + 38;
quote.fontSize = 14;
quote.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.88 }];
quote.resize(CONTENT_W - 40, 10);
created.push(quote);

const author = penpot.createText("— Client vérifié · 5 étoiles");
author.name = "Testimonial/Author";
author.x = MARGIN + 20;
author.y = quote.y + quote.height + 10;
author.fontSize = 12;
author.fontWeight = "bold";
author.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.6 }];
author.resize(CONTENT_W - 40, 10);
created.push(author);

const totalH = author.y + author.height - flowY + 18;
card.resize(CONTENT_W, totalH);
accentBar.resize(4, totalH);
flowY += totalH + 20;
