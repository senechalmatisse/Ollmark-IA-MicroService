const TEXT_COLOR = '{{textColor}}';
const COLUMN_WIDTH = 620;

function createTextBlock(content, size, weight, opacity, spacing) {
    const txt = penpot.createText(content);
    txt.x = columnX;
    txt.y = flowY;
    txt.fontSize   = size;
    txt.fontWeight = weight;
    txt.fills = [{ fillColor: TEXT_COLOR, fillOpacity: opacity }];
    txt.resize(COLUMN_WIDTH, 10);
    created.push(txt);
    flowY += txt.height + spacing;
}

if ({{hasTitle}}) createTextBlock('{{title}}', {{titleSize}}, "bold", 1, 80);
if ({{hasSubtitle}}) createTextBlock('{{subtitle}}', 22, "normal", 0.9,  40);
if ({{hasParagraph}}) createTextBlock('{{paragraph}}', 18, "normal", 0.75, 60);

const newHeight = flowY - section.y + 100;
if (newHeight > section.height) section.resize(section.width, newHeight);