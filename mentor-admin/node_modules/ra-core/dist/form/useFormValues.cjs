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
Object.defineProperty(exports, "__esModule", { value: true });
exports.useFormValues = void 0;
var react_hook_form_1 = require("react-hook-form");
// hook taken from https://react-hook-form.com/docs/usewatch/#rules
var useFormValues = function () {
    var getValues = (0, react_hook_form_1.useFormContext)().getValues;
    return __assign(__assign({}, (0, react_hook_form_1.useWatch)()), getValues());
};
exports.useFormValues = useFormValues;
//# sourceMappingURL=useFormValues.js.map