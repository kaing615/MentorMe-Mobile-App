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
import { styled, useThemeProps, } from '@mui/material/styles';
import { Box, LinearProgress as MuiLinearProgress, } from '@mui/material';
import { useTimeout } from 'ra-core';
/**
 * Progress bar formatted to replace an input or a field in a form layout
 *
 * Avoids visual jumps when replaced by value or form input
 *
 * @see ReferenceField
 * @see ReferenceInput
 *
 * @typedef {Object} Props the props you can use
 * @prop {Object} classes CSS class names
 * @prop {string} className CSS class applied to the LinearProgress component
 * @prop {integer} timeout Milliseconds to wait before showing the progress bar. One second by default
 *
 * @param {Props} props
 */
export var LinearProgress = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, _a = props.timeout, timeout = _a === void 0 ? 1000 : _a, rest = __rest(props, ["className", "timeout"]);
    var oneSecondHasPassed = useTimeout(timeout);
    return oneSecondHasPassed ? (React.createElement(StyledProgress, __assign({ className: className }, rest))) : (React.createElement(Box, { component: "span", sx: {
            my: 1,
            height: 4,
            display: 'block',
        } }));
};
// What? TypeScript loses the displayName if we don't set it explicitly
LinearProgress.displayName = 'LinearProgress';
var PREFIX = 'RaLinearProgress';
var StyledProgress = styled(MuiLinearProgress, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var theme = _a.theme;
    return ({
        margin: "".concat(theme.spacing(1), " 0"),
        width: theme.spacing(20),
    });
});
//# sourceMappingURL=LinearProgress.js.map