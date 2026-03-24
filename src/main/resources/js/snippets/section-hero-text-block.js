const TEXT_COLOR = '{{textColor}}';
const COLUMN_WIDTH = {{columnWidth}};

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

if ({{hasTitle}})     createTextBlock('{{title}}',    {{titleSize}},    "bold",   1,    20);
if ({{hasSubtitle}})  createTextBlock('{{subtitle}}',  {{subtitleSize}}, "normal", 0.88, 16);
if ({{hasParagraph}}) createTextBlock('{{paragraph}}', {{paragraphSize}},"normal", 0.68, 40);

const newHeight = flowY - section.y + 100;
if (newHeight > section.height) section.resize(section.width, newHeight);
