import * as React from 'react';
import { Typography } from '@mui/material';
import { useTranslate, useSetInspectorTitle } from 'ra-core';
export var InspectorRoot = function () {
    var translate = useTranslate();
    useSetInspectorTitle('ra.configurable.inspector.title', {
        _: 'Inspector',
    });
    return (React.createElement(Typography, null, translate('ra.configurable.inspector.content', {
        _: 'Hover the application UI elements to configure them',
    })));
};
//# sourceMappingURL=InspectorRoot.js.map