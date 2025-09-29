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
var _a;
Object.defineProperty(exports, "__esModule", { value: true });
exports.CheckboxGroupInputItemClasses = exports.CheckboxGroupInputItem = void 0;
var React = __importStar(require("react"));
var styles_1 = require("@mui/material/styles");
var material_1 = require("@mui/material");
var ra_core_1 = require("ra-core");
var CheckboxGroupInputItem = function (inProps) {
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var id = props.id, choice = props.choice, className = props.className, fullWidth = props.fullWidth, onChange = props.onChange, optionText = props.optionText, optionValue = props.optionValue, options = props.options, translateChoice = props.translateChoice, value = props.value, labelPlacement = props.labelPlacement, inputRef = props.inputRef, _a = props.disableValue, disableValue = _a === void 0 ? 'disabled' : _a, rest = __rest(props, ["id", "choice", "className", "fullWidth", "onChange", "optionText", "optionValue", "options", "translateChoice", "value", "labelPlacement", "inputRef", "disableValue"]);
    var _b = (0, ra_core_1.useChoices)({
        optionText: optionText,
        optionValue: optionValue,
        translateChoice: translateChoice,
        disableValue: disableValue,
    }), getChoiceText = _b.getChoiceText, getChoiceValue = _b.getChoiceValue, getDisableValue = _b.getDisableValue;
    var choiceName = getChoiceText(choice);
    var disabled = getDisableValue(choice);
    return (React.createElement(StyledFormControlLabel, __assign({ htmlFor: "".concat(id, "_").concat(getChoiceValue(choice)), key: getChoiceValue(choice), onChange: onChange, className: className, inputRef: inputRef, control: React.createElement(material_1.Checkbox, __assign({ id: "".concat(id, "_").concat(getChoiceValue(choice)), color: "primary", className: exports.CheckboxGroupInputItemClasses.checkbox, checked: value
                ? value.find(function (v) { return v == getChoiceValue(choice); }) !== // eslint-disable-line eqeqeq
                    undefined
                : false, value: String(getChoiceValue(choice)), disabled: disabled }, options)), label: choiceName, labelPlacement: labelPlacement }, rest)));
};
exports.CheckboxGroupInputItem = CheckboxGroupInputItem;
var PREFIX = 'RaCheckboxGroupInputItem';
exports.CheckboxGroupInputItemClasses = {
    checkbox: "".concat(PREFIX, "-checkbox"),
};
var StyledFormControlLabel = (0, styles_1.styled)(material_1.FormControlLabel, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {},
    _a["& .".concat(exports.CheckboxGroupInputItemClasses.checkbox)] = {
        height: 32,
    },
    _a));
//# sourceMappingURL=CheckboxGroupInputItem.js.map