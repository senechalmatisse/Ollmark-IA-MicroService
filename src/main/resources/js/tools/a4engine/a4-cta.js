const CTA_SPACING = 40;
flowY += CTA_SPACING;

function createA4Button(label, bg, fg, isSecondary) {
    const MIN_W = isSecondary ? 200 : 260;
    const MAX_W = 440;
    const estimatedWidth = label.length * 11;
    const width = Math.min(MAX_W, Math.max(MIN_W, estimatedWidth + 80));

    const rect = penpot.createRectangle();
    rect.name = isSecondary ? "CTA/Secondary" : "CTA/Primary";
    rect.resize(width, 62);
    rect.x = PAGE_W / 2 - width / 2;
    rect.y = flowY;
    rect.borderRadius = 16;
    rect.fills = [{ fillColor: bg }];

    if (isSecondary) {
        rect.fills = [{ fillColor: "#FFFFFF", fillOpacity: 0.18 }];
        rect.strokes = [{ strokeColor: fg, strokeWidth: 1.5, strokeOpacity: 0.6 }];
    }
    created.push(rect);

    const text = penpot.createText(label);
    text.name = "CTA/Label";
    text.fontSize = 20;
    text.fontWeight = "bold";
    text.fills = [{ fillColor: fg }];
    text.resize(width, 40);
    const tw = (text.width && text.width > 0) ? text.width : estimatedTextWidth;
    text.x = rect.x + (width - tw) / 2;
    text.y = rect.y + (62 - text.height) / 2;
    created.push(text);

    return 62;
}

if ("{{primary}}".length > 0) {
    const h = createA4Button("{{primary}}", "{{primaryBg}}", "{{primaryText}}", false);
    flowY += h + 18;
}

if ("{{secondary}}".length > 0) {
    const h = createA4Button("{{secondary}}", "rgba(255,255,255,0.18)", "{{secondaryText}}", true);
    flowY += h;
}

flowY += 60;
