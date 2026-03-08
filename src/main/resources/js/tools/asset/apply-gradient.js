shape.fills = [{ fillColorGradient: {
    type: 'linear',
    startX: {{startX}}, startY: {{startY}},
    endX: {{endX}},     endY: {{endY}},
    width: 1,
    stops: [
        { color: '{{startColor}}', opacity: 1, offset: 0 },
        { color: '{{endColor}}',   opacity: 1, offset: 1 }
    ]
}}];
return { id: shape.id, gradient: 'linear', angle: {{angle}} };