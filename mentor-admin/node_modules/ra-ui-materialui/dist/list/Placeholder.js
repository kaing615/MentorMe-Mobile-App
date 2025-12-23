var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
import * as React from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
export var Placeholder = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    return React.createElement(Root, __assign({}, props), "\u00A0");
};
var PREFIX = 'RaPlaceholder';
var Root = styled('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var theme = _a.theme;
    return ({
        backgroundColor: (theme.vars || theme).palette.grey[300],
        display: 'flex',
    });
});
//# sourceMappingURL=Placeholder.js.map