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
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
import * as React from 'react';
import { useMediaQuery, Toolbar, } from '@mui/material';
import { styled, useThemeProps, } from '@mui/material/styles';
export var TopToolbar = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var isXSmall = useMediaQuery(function (theme) {
        return theme.breakpoints.down('sm');
    });
    return (React.createElement(StyledToolbar, __assign({ disableGutters: true, variant: isXSmall ? 'regular' : 'dense' }, sanitizeToolbarRestProps(props))));
};
export default TopToolbar;
var PREFIX = 'RaTopToolbar';
var StyledToolbar = styled(Toolbar, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'flex-end',
            gap: theme.spacing(1),
            whiteSpace: 'nowrap',
            flex: '0 1 auto',
            padding: theme.spacing(0.5),
            paddingBottom: theme.spacing(1)
        },
        _b[theme.breakpoints.down('md')] = {
            flex: '0 1 100%',
        },
        _b[theme.breakpoints.down('sm')] = {
            backgroundColor: (theme.vars || theme).palette.background.paper,
            padding: 0,
            paddingBottom: 0,
        },
        _b);
});
var sanitizeToolbarRestProps = function (_a) {
    var hasCreate = _a.hasCreate, props = __rest(_a, ["hasCreate"]);
    return props;
};
//# sourceMappingURL=TopToolbar.js.map