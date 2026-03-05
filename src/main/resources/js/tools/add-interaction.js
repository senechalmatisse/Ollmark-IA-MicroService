const action = {{actionJs}};
const interaction = shape.addInteraction('{{trigger}}', action{{delayArg}});
return {
    id: shape.id,
    interactionCount: shape.interactions.length,
    trigger: '{{trigger}}',
    action: '{{actionType}}'
};