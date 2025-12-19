export const PRODUCT_CATEGORIES = [
    { value: 'EDUCATION', label: 'Educational Toy', description: 'Learning and development toys' },
    { value: 'ELECTRONIC', label: 'Electronic Toy', description: 'Battery-operated and electronic gadgets' },
    { value: 'ACTION_FIGURES', label: 'Action Figure', description: 'Superhero and character figures' },
    { value: 'DOLLS', label: 'Dolls', description: 'Dolls and doll accessories' },
    { value: 'MUSICAL_TOY', label: 'Musical Toy', description: 'Instruments and music-making toys' },
    { value: 'OUTDOOR_TOY', label: 'Outdoor Toy', description: 'Sports and outdoor play equipment' },
];

export const PRODUCT_STATUS = [
    { value: 'ACTIVE', label: 'Active', color: '#4caf50', active: true },
    { value:  'ON_ORDER', label:  'On Order', color: '#2196f3', active: true },
    { value: 'PLANNING', label: 'Planning', color: '#ff9800', active: false },
    { value:  'DISCONTINUED', label: 'Discontinued', color: '#f44336', active: false },
    { value: 'ARCHIVED', label: 'Archived', color: '#757575', active: false },
    { value: 'RESTRICTED', label: 'Restricted', color: '#9c27b0', active: false },
];

export const getCategoryLabel = (value) => {
    const category = PRODUCT_CATEGORIES.find(cat => cat.value === value);
    return category ? category.label : value;
};

export const getStatusLabel = (value) => {
    const status = PRODUCT_STATUS.find(s => s.value === value);
    return status ? status.label :  value;
};

export const getStatusColor = (value) => {
    const status = PRODUCT_STATUS.find(s => s.value === value);
    return status ? status.color :  '#757575';
};

export const getCategoryColor = (value) => {
    const colors = {
        EDUCATION: '#4CAF50',
        ELECTRONIC:  '#2196F3',
        ACTION_FIGURES: '#FF5722',
        DOLLS:  '#E91E63',
        MUSICAL_TOY: '#9C27B0',
        OUTDOOR_TOY: '#FF9800',
    };
    return colors[value] || '#757575';
};