flowY += 20;

const testimonial = penpot.createRectangle();
testimonial.resize(CONTENT_W, 90);
testimonial.x = MARGIN;
testimonial.y = flowY;
testimonial.borderRadius = 14;
testimonial.fills = [{ fillColor: "{{textColor}}", fillOpacity: 0.08 }];
created.push(testimonial);

const quote = penpot.createText("Une expérience incroyable, je recommande ce produit.");
quote.x = MARGIN + 20;
quote.y = flowY + 35;
quote.fontSize = 14;
quote.fills = [{ fillColor: "{{textColor}}" }];
created.push(quote);

flowY += 120;