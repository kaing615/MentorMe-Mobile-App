"use strict";
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
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
var _a;
Object.defineProperty(exports, "__esModule", { value: true });
exports.ResettableTextFieldStyles = exports.ResettableTextFieldClasses = exports.ResettableTextField = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
var styles_1 = require("@mui/material/styles");
var clsx_1 = __importDefault(require("clsx"));
var material_1 = require("@mui/material");
var Clear_1 = __importDefault(require("@mui/icons-material/Clear"));
var ra_core_1 = require("ra-core");
/**
 * An override of the default Material UI TextField which is resettable
 */
exports.ResettableTextField = (0, react_1.forwardRef)(function (inProps, ref) {
    // This ensures we inherit the theme props from the TextField component (variant for instance)
    var textFieldProps = (0, styles_1.useThemeProps)({
        props: inProps,
        name: 'MuiTextField',
    });
    var props = (0, styles_1.useThemeProps)({
        props: textFieldProps,
        name: PREFIX,
    });
    var clearAlwaysVisible = props.clearAlwaysVisible, InputProps = props.InputProps, value = props.value, resettable = props.resettable, disabled = props.disabled, readOnly = props.readOnly, variant = props.variant, margin = props.margin, className = props.className, rest = __rest(props, ["clearAlwaysVisible", "InputProps", "value", "resettable", "disabled", "readOnly", "variant", "margin", "className"]);
    var translate = (0, ra_core_1.useTranslate)();
    var onChange = props.onChange;
    var handleClickClearButton = (0, react_1.useCallback)(function (event) {
        event.preventDefault();
        onChange && onChange('');
    }, [onChange]);
    var clearButton = exports.ResettableTextFieldClasses.clearButton, clearIcon = exports.ResettableTextFieldClasses.clearIcon, inputAdornedEnd = exports.ResettableTextFieldClasses.inputAdornedEnd, selectAdornment = exports.ResettableTextFieldClasses.selectAdornment, visibleClearIcon = exports.ResettableTextFieldClasses.visibleClearIcon;
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
                return (React.createElement(material_1.InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                    React.createElement(material_1.IconButton, { className: clearButton, "aria-label": translate('ra.action.clear_input_value'), title: translate('ra.action.clear_input_value'), disabled: true, size: "large" },
                        React.createElement(Clear_1.default, { className: (0, clsx_1.default)(clearIcon, visibleClearIcon) }))));
            }
            else {
                if (endAdornment) {
                    return endAdornment;
                }
                else {
                    // show spacer
                    return (React.createElement(material_1.InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                        React.createElement("span", { className: clearButton }, "\u00A0")));
                }
            }
        }
        else {
            // show clear
            return (React.createElement(material_1.InputAdornment, { position: "end", className: props.select ? selectAdornment : undefined },
                React.createElement(material_1.IconButton, { className: clearButton, "aria-label": translate('ra.action.clear_input_value'), title: translate('ra.action.clear_input_value'), onClick: handleClickClearButton, onMouseDown: handleMouseDownClearButton, disabled: disabled || readOnly, size: "large" },
                    React.createElement(Clear_1.default, { className: (0, clsx_1.default)(clearIcon, (_a = {},
                            _a[visibleClearIcon] = clearAlwaysVisible || value,
                            _a)) }))));
        }
    };
    return (React.createElement(StyledTextField, __assign({ value: value, InputProps: __assign({ readOnly: readOnly, classes: props.select && variant === 'filled'
                ? { adornedEnd: inputAdornedEnd }
                : {}, endAdornment: getEndAdornment() }, InputPropsWithoutEndAdornment), disabled: disabled || readOnly, variant: variant, margin: margin, className: className }, rest, { inputRef: ref })));
});
exports.ResettableTextField.displayName = 'ResettableTextField';
var handleMouseDownClearButton = function (event) {
    event.preventDefault();
};
var PREFIX = 'RaResettableTextField';
exports.ResettableTextFieldClasses = {
    clearIcon: "".concat(PREFIX, "-clearIcon"),
    visibleClearIcon: "".concat(PREFIX, "-visibleClearIcon"),
    clearButton: "".concat(PREFIX, "-clearButton"),
    selectAdornment: "".concat(PREFIX, "-selectAdornment"),
    inputAdornedEnd: "".concat(PREFIX, "-inputAdornedEnd"),
};
exports.ResettableTextFieldStyles = (_a = {},
    _a["& .".concat(exports.ResettableTextFieldClasses.clearIcon)] = {
        height: 16,
        width: 0,
    },
    _a["& .".concat(exports.ResettableTextFieldClasses.visibleClearIcon)] = {
        width: 16,
    },
    _a["& .".concat(exports.ResettableTextFieldClasses.clearButton)] = {
        height: 24,
        width: 24,
        padding: 0,
    },
    _a["& .".concat(exports.ResettableTextFieldClasses.selectAdornment)] = {
        position: 'absolute',
        right: 24,
    },
    _a["& .".concat(exports.ResettableTextFieldClasses.inputAdornedEnd)] = {
        paddingRight: 0,
    },
    _a);
var StyledTextField = (0, styles_1.styled)(material_1.TextField, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(exports.ResettableTextFieldStyles);
//# sourceMappingURL=ResettableTextField.js.map