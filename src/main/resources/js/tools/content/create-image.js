const imageData = await penpot.uploadMediaUrl('IA-Upload', '{{url}}');
const rect = penpot.createRectangle();
rect.resize({{width}}, {{height}});
rect.x = {{x}};
rect.y = {{y}};
rect.fills = [{ fillOpacity: 1, fillImage: imageData }];
return rect.id;