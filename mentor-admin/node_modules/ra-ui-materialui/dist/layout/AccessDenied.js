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
var _a;
import * as React from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import LockIcon from '@mui/icons-material/Lock';
import { Typography } from '@mui/material';
import clsx from 'clsx';
import { useTranslate } from 'ra-core';
export var AccessDenied = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, _a = props.icon, icon = _a === void 0 ? DEFAULT_ICON : _a, _b = props.textPrimary, textPrimary = _b === void 0 ? 'ra.page.access_denied' : _b, _c = props.textSecondary, textSecondary = _c === void 0 ? 'ra.message.access_denied' : _c, rest = __rest(props, ["className", "icon", "textPrimary", "textSecondary"]);
    var translate = useTranslate();
    return (React.createElement(Root, __assign({ className: clsx(AccessDeniedClasses.root, className) }, rest),
        React.createElement("div", { className: AccessDeniedClasses.message },
            icon,
            React.createElement(Typography, { variant: "h5", sx: {
                    mt: 3,
                    color: 'text.secondary',
                } }, translate(textPrimary, { _: textPrimary })),
            React.createElement(Typography, { variant: "body2" }, translate(textSecondary, {
                _: textSecondary,
            })))));
};
var PREFIX = 'RaAccessDenied';
export var AccessDeniedClasses = {
    root: "".concat(PREFIX, "-root"),
    icon: "".concat(PREFIX, "-icon"),
    message: "".concat(PREFIX, "-message"),
};
var Root = styled('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%'
    },
    _a["& .".concat(AccessDeniedClasses.message)] = {
        textAlign: 'center',
        paddingTop: '1em',
        paddingBottom: '1em',
        opacity: 0.5,
    },
    _a["& .".concat(AccessDeniedClasses.icon)] = {
        width: '9em',
        height: '9em',
    },
    _a));
var DEFAULT_ICON = React.createElement(LockIcon, { className: AccessDeniedClasses.icon });
//# sourceMappingURL=AccessDenied.js.map