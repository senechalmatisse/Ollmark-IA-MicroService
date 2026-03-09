const imageData = await penpot.uploadMediaUrl('IA-Replace', '{{imageUrl}}');
if (!imageData) throw new Error('Failed to upload image from URL: {{imageUrl}}');
shape.fills = [{ fillOpacity: 1, fillImage: { ...imageData, keepAspectRatio: {{keepAspectRatio}} } }];
return {
    id: shape.id,
    imageId: imageData.id,
    width: imageData.width,
    height: imageData.height
};