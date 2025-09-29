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
import { forwardRef, useCallback } from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import clsx from 'clsx';
import { InputAdornment, IconButton, TextField as MuiTextField, } from '@mui/material';
import ClearIcon from '@mui/icons-material/Clear';
import { useTranslate } from 'ra-core';
/**
 * An override of the default Material UI TextField which is resettable
 */
export var ResettableTextField = forwardRef(function (inProps, ref) {
    // This ensures we inherit the theme props from the TextField component (variant for instance)
    var textFieldProps = useThemeProps({
        props: inProps,
        name: 'MuiTextField',
    });
    var props = useThemeProps({
        props: textFieldProps,
        name: PREFIX,
    });
    var clearAlwaysVisible = props.clearAlwaysVisible, InputProps = props.InputProps, value = props.value, resettable = props.resettable, disabled = props.disabled, readOnly = props.readOnly, variant = props.variant, margin = props.margin, className = props.className, rest = __rest(props, ["clearAlwaysVisible", "InputProps", "value", "resettable", "disabled", "readOnly", "variant", "margin", "className"]);
    var translate = useTranslate();
    var onChange = props.onChange;
    var handleClickClearButton = useCallback(function (event) {
        event.preventDefault();
        onChange && onChange('');
    }, [onChange]);
    var clearButton = ResettableTextFieldClasses.clearButton, clearIcon = ResettableTextFieldClasses.clearIcon, inputAdornedEnd = ResettableTextFieldClasses.inputAdornedEnd, selectAdornment = ResettableTextFieldClasses.selectAdornment, visibleClearIcon = ResettableTextFieldClasses.visibleClearIcon;
    var _a = InputProps || {}, endAdornment = _a.endAdornment, InputPropsWithoutEndAdornment = __rest(_a, ["endAdornment"]);
    if (clearAlwaysVisible && endAdornment) {
        throw new Error('ResettableTextField cannot display both an endAdornment and a clear button always visible');
    }
    var getEndAdornment = function () {
        var _a;
        if (!resettable) {
            return endAdornment;
        }
        else if (!value) {
            if (clearAlwaysVisible) {
                // show clear button, inactive
                return (React.createElement(InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                    React.createElement(IconButton, { className: clearButton, "aria-label": translate('ra.action.clear_input_value'), title: translate('ra.action.clear_input_value'), disabled: true, size: "large" },
                        React.createElement(ClearIcon, { className: clsx(clearIcon, visibleClearIcon) }))));
            }
            else {
                if (endAdornment) {
                    return endAdornment;
                }
                else {
                    // show spacer
                    return (React.createElement(InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                        React.createElement("span", { className: clearButton }, "\u00A0")));
                }
            }
        }
        else {
            // show clear
            return (React.createElement(InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                React.createElement(IconButton, { className: clearButton, "aria-label": translate('ra.action.clear_input_value'), title: translate('ra.action.clear_input_value'), onClick: handleClickClearButton, onMouseDown: handleMouseDownClearButton, disabled: disabled || readOnly, size: "large" },
                    React.createElement(ClearIcon, { className: clsx(clearIcon, (_a = {},
                            _a[visibleClearIcon] = clearAlwaysVisible || value,
                            _a)) }))));
        }
    };
    return (React.createElement(StyledTextField, __assign({ value: value, InputProps: __assign({ readOnly: readOnly, classes: props.select && variant === 'filled'
                ? { adornedEnd: inputAdornedEnd }
                : {}, endAdornment: getEndAdornment() }, InputPropsWithoutEndAdornment), disabled: disabled || readOnly, variant: variant, margin: margin, className: className }, rest, { inputRef: ref })));
});
ResettableTextField.displayName = 'ResettableTextField';
var handleMouseDownClearButton = function (event) {
    event.preventDefault();
};
var PREFIX = 'RaResettableTextField';
export var ResettableTextFieldClasses = {
    clearIcon: "".concat(PREFIX, "-clearIcon"),
    visibleClearIcon: "".concat(PREFIX, "-visibleClearIcon"),
    clearButton: "".concat(PREFIX, "-clearButton"),
    selectAdornment: "".concat(PREFIX, "-selectAdornment"),
    inputAdornedEnd: "".concat(PREFIX, "-inputAdornedEnd"),
};
export var ResettableTextFieldStyles = (_a = {},
    _a["& .".concat(ResettableTextFieldClasses.clearIcon)] = {
        height: 16,
        width: 0,
    },
    _a["& .".concat(ResettableTextFieldClasses.visibleClearIcon)] = {
        width: 16,
    },
    _a["& .".concat(ResettableTextFieldClasses.clearButton)] = {
        height: 24,
        width: 24,
        padding: 0,
    },
    _a["& .".concat(ResettableTextFieldClasses.selectAdornment)] = {
        position: 'absolute',
        right: 24,
    },
    _a["& .".concat(ResettableTextFieldClasses.inputAdornedEnd)] = {
        paddingRight: 0,
    },
    _a);
var StyledTextField = styled(MuiTextField, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(ResettableTextFieldStyles);
//# sourceMappingURL=ResettableTextField.js.map