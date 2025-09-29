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
import { useEffect } from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import RemoveCircle from '@mui/icons-material/RemoveCircle';
import IconButton from '@mui/material/IconButton';
import { useTranslate } from 'ra-core';
export var FileInputPreview = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, className = props.className, onRemove = props.onRemove, file = props.file, _a = props.removeIcon, RemoveIcon = _a === void 0 ? RemoveCircle : _a, rest = __rest(props, ["children", "className", "onRemove", "file", "removeIcon"]);
    var translate = useTranslate();
    useEffect(function () {
        return function () {
            var preview = file.rawFile ? file.rawFile.preview : file.preview;
            if (preview) {
                window.URL.revokeObjectURL(preview);
            }
        };
    }, [file]);
    return (React.createElement(Root, __assign({ className: className }, rest),
        React.createElement(IconButton, { className: FileInputPreviewClasses.removeButton, onClick: onRemove, "aria-label": translate('ra.action.delete'), title: translate('ra.action.delete'), size: "small" },
            React.createElement(RemoveIcon, { className: FileInputPreviewClasses.removeIcon })),
        children));
};
var PREFIX = 'RaFileInputPreview';
var FileInputPreviewClasses = {
    removeButton: "".concat(PREFIX, "-removeButton"),
    removeIcon: "".concat(PREFIX, "-removeIcon"),
};
var Root = styled('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(FileInputPreviewClasses.removeButton)] = {},
        _b["& .".concat(FileInputPreviewClasses.removeIcon)] = {
            color: (theme.vars || theme).palette.error.main,
        },
        _b);
});
//# sourceMappingURL=FileInputPreview.js.map