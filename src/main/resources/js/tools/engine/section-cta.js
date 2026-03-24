const CTA_SPACING = 60;
const CTA_HEIGHT = 60;
const CTA_GAP = 24;
const MIN_WIDTH = 220;
const PADDING_X = 36;
const FONT_SIZE = 18;

flowY += CTA_SPACING;

function estimateTextWidth(label) {
    return label.length * FONT_SIZE * 0.55;
}

function createButton(label, bg, fg, offsetX, isPrimary) {
    const textWidth = estimateTextWidth(label);
    const width     = Math.max(MIN_WIDTH, textWidth + PADDING_X * 2);

    const rect = penpot.createRectangle();
    rect.name = isPrimary ? "CTA/Primary" : "CTA/Secondary";
    rect.x = columnX + offsetX;
    rect.y = flowY;
    rect.resize(width, CTA_HEIGHT);
    rect.borderRadius = 18;

    if (isPrimary) {
        rect.fills = [{ fillColor: bg, fillOpacity: 1 }];
    } else {
        rect.fills = [{ fillColor: "#FFFFFF", fillOpacity: 1 }];
        rect.strokes = [{ strokeColor: fg, strokeWidth: 1, strokeOpacity: 0.4 }];
    }
    created.push(rect);

    const text = penpot.createText(label);
    text.name = "CTA/Label";
    text.fontSize = FONT_SIZE;
    text.fontWeight = "bold";
    text.fills = [{ fillColor: fg, fillOpacity: 1 }];
    text.resize(width, 10);
    const tw = (text.width && text.width > 0) ? text.width : estimatedTextWidth;
    text.x = rect.x + (width - tw) / 2;
    text.y = rect.y + (CTA_HEIGHT - text.height) / 2 - 1;
    created.push(text);

    return width;
}

let offset = 0;
const hasPrimary = "{{primary}}".length > 0;
const hasSecondary = "{{secondary}}".length > 0;

let primaryWidth = 0;
let secondaryWidth = 0;

if (hasPrimary) primaryWidth = Math.max(MIN_WIDTH, estimateTextWidth("{{primary}}") + PADDING_X * 2);
if (hasSecondary) secondaryWidth = Math.max(MIN_WIDTH, estimateTextWidth("{{secondary}}") + PADDING_X * 2);

const totalWidth =
    (hasPrimary ? primaryWidth : 0) +
    (hasSecondary ? secondaryWidth : 0) +
    (hasPrimary && hasSecondary ? CTA_GAP : 0);

offset = (COMPONENT_WIDTH - totalWidth) / 2;

if (hasPrimary) {
    const w = createButton('{{primary}}', "{{primaryBg}}", "{{primaryText}}", offset, true);
    offset += w + CTA_GAP;
}

if (hasSecondary) createButton('{{secondary}}', "", "{{secondaryText}}", offset, false);

flowY += CTA_HEIGHT + 80;