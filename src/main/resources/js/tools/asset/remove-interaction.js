const interactions = shape.interactions;
if ({{index}} >= interactions.length) {
    throw new Error('Interaction index {{index}} out of bounds (shape has ' + interactions.length + ' interactions)');
}
const interaction = interactions[{{index}}];
shape.removeInteraction(interaction);
return {
    id: shape.id,
    removedIndex: {{index}},
    remainingCount: shape.interactions.length
};