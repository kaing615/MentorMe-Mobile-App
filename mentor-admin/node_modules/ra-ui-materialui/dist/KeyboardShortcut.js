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
import { styled, Typography, } from '@mui/material';
import clsx from 'clsx';
export var KeyboardShortcut = function (_a) {
    var className = _a.className, keyboardShortcut = _a.keyboardShortcut, rest = __rest(_a, ["className", "keyboardShortcut"]);
    if (!keyboardShortcut) {
        return null;
    }
    return (React.createElement(Root, __assign({ className: clsx(KeyboardShortcutClasses.root, className) }, rest), keyboardShortcut
        .split('>')
        .map(function (sequence, sequenceIndex, sequences) { return (React.createElement(React.Fragment, { key: "".concat(sequence, "-").concat(sequenceIndex) },
        sequence.split('+').map(function (key, keyIndex) { return (React.createElement(React.Fragment, { key: "".concat(key, "-").concat(keyIndex) },
            React.createElement(Typography, { component: "kbd", className: KeyboardShortcutClasses.kbd, key: key }, KeyMap[key]
                ? KeyMap[key]
                : key.toUpperCase()))); }),
        sequenceIndex < sequences.length - 1 ? (React.createElement(React.Fragment, null, "\u00A0")) : null)); })));
};
var KeyMap = {
    meta: '⌘',
    mod: '⌘',
    ctrl: '⌃',
    shift: '⇧',
    alt: '⌥',
    enter: '⏎',
    esc: '⎋',
    escape: '⎋',
    backspace: '⌫',
    delete: '⌦',
    tab: '⇥',
    space: '␣',
    up: '↑',
    down: '↓',
    left: '←',
    right: '→',
    home: '↖',
    end: '↘',
    pageup: '⇞',
    pagedown: '⇟',
};
var PREFIX = 'RaKeyboardShortcut';
var KeyboardShortcutClasses = {
    root: "".concat(PREFIX, "-root"),
    kbd: "".concat(PREFIX, "-kbd"),
};
var Root = styled('div')(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            opacity: 0.7
        },
        _b["& .".concat(KeyboardShortcutClasses.kbd)] = {
            padding: '4px 5px',
            display: 'inline-block',
            whiteSpace: 'nowrap',
            margin: '0 1px',
            fontSize: '11px',
            lineHeight: '10px',
            color: theme.palette.text.primary,
            verticalAlign: 'middle',
            border: "1px solid ".concat(theme.palette.divider),
            borderRadius: 6,
            boxShadow: "inset 0 -1px 0 ".concat(theme.palette.divider),
        },
        _b);
});
//# sourceMappingURL=KeyboardShortcut.js.map