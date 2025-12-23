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
import { Card, styled, } from '@mui/material';
import { useThemeProps } from '@mui/system';
import clsx from 'clsx';
export var AuthLayout = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, className = props.className, rest = __rest(props, ["children", "className"]);
    return (React.createElement(Root, __assign({ className: clsx(AuthLayoutClasses.root, className) }, rest),
        React.createElement(Card, { className: AuthLayoutClasses.card }, children)));
};
var PREFIX = 'RaAuthLayout';
export var AuthLayoutClasses = {
    root: "".concat(PREFIX, "-root"),
    card: "".concat(PREFIX, "-card"),
};
var Root = styled('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function () {
    var _a;
    return (_a = {
            display: 'flex',
            flexDirection: 'column',
            minHeight: '100vh',
            height: '1px',
            alignItems: 'center',
            justifyContent: 'flex-start',
            backgroundRepeat: 'no-repeat',
            backgroundSize: 'cover',
            backgroundImage: 'radial-gradient(circle at 50% 14em, #313264 0%, #00023b 60%, #00023b 100%)'
        },
        _a["& .".concat(AuthLayoutClasses.card)] = {
            minWidth: 300,
            marginTop: '6em',
        },
        _a);
});
//# sourceMappingURL=AuthLayout.js.map