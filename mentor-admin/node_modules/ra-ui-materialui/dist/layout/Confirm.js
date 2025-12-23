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
import { useCallback } from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, } from '@mui/material';
import { styled, useThemeProps, } from '@mui/material/styles';
import ActionCheck from '@mui/icons-material/CheckCircle';
import AlertError from '@mui/icons-material/ErrorOutline';
import clsx from 'clsx';
import { useTranslate } from 'ra-core';
/**
 * Confirmation dialog
 *
 * @example
 * <Confirm
 *     isOpen={true}
 *     title="Delete Item"
 *     content="Are you sure you want to delete this item?"
 *     confirm="Yes"
 *     confirmColor="primary"
 *     ConfirmIcon=ActionCheck
 *     CancelIcon=AlertError
 *     cancel="Cancel"
 *     onConfirm={() => { // do something }}
 *     onClose={() => { // do something }}
 * />
 */
export var Confirm = function (inProps) {
    var _a;
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, _b = props.isOpen, isOpen = _b === void 0 ? false : _b, loading = props.loading, title = props.title, content = props.content, _c = props.cancel, cancel = _c === void 0 ? 'ra.action.cancel' : _c, _d = props.confirm, confirm = _d === void 0 ? 'ra.action.confirm' : _d, _e = props.confirmColor, confirmColor = _e === void 0 ? 'primary' : _e, _f = props.ConfirmIcon, ConfirmIcon = _f === void 0 ? ActionCheck : _f, _g = props.CancelIcon, CancelIcon = _g === void 0 ? AlertError : _g, onClose = props.onClose, onConfirm = props.onConfirm, _h = props.translateOptions, translateOptions = _h === void 0 ? {} : _h, _j = props.titleTranslateOptions, titleTranslateOptions = _j === void 0 ? translateOptions : _j, _k = props.contentTranslateOptions, contentTranslateOptions = _k === void 0 ? translateOptions : _k, rest = __rest(props, ["className", "isOpen", "loading", "title", "content", "cancel", "confirm", "confirmColor", "ConfirmIcon", "CancelIcon", "onClose", "onConfirm", "translateOptions", "titleTranslateOptions", "contentTranslateOptions"]);
    var translate = useTranslate();
    var handleConfirm = useCallback(function (e) {
        e.stopPropagation();
        onConfirm(e);
    }, [onConfirm]);
    var handleClick = useCallback(function (e) {
        e.stopPropagation();
    }, []);
    return (React.createElement(StyledDialog, __assign({ className: className, open: isOpen, onClose: onClose, onClick: handleClick, "aria-labelledby": "alert-dialog-title" }, rest),
        React.createElement(DialogTitle, { id: "alert-dialog-title" }, typeof title === 'string'
            ? translate(title, __assign({ _: title }, titleTranslateOptions))
            : title),
        React.createElement(DialogContent, null, typeof content === 'string' ? (React.createElement(DialogContentText, null, translate(content, __assign({ _: content }, contentTranslateOptions)))) : (content)),
        React.createElement(DialogActions, null,
            React.createElement(Button, { disabled: loading, onClick: onClose, startIcon: React.createElement(CancelIcon, null) }, translate(cancel, { _: cancel })),
            React.createElement(Button, { disabled: loading, onClick: handleConfirm, className: clsx('ra-confirm', (_a = {},
                    _a[ConfirmClasses.confirmWarning] = confirmColor === 'warning',
                    _a[ConfirmClasses.confirmPrimary] = confirmColor === 'primary',
                    _a)), autoFocus: true, startIcon: React.createElement(ConfirmIcon, null) }, translate(confirm, { _: confirm })))));
};
var PREFIX = 'RaConfirm';
export var ConfirmClasses = {
    confirmPrimary: "".concat(PREFIX, "-confirmPrimary"),
    confirmWarning: "".concat(PREFIX, "-confirmWarning"),
};
var StyledDialog = styled(Dialog, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(ConfirmClasses.confirmPrimary)] = {
            color: (theme.vars || theme).palette.primary.main,
        },
        _b["& .".concat(ConfirmClasses.confirmWarning)] = {
            color: (theme.vars || theme).palette.error.main,
            '&:hover': {
                backgroundColor: "color-mix(in srgb, ".concat((theme.vars || theme).palette.error.main, ", transparent 12%)"),
                // Reset on mouse devices
                '@media (hover: none)': {
                    backgroundColor: 'transparent',
                },
            },
        },
        _b);
});
//# sourceMappingURL=Confirm.js.map