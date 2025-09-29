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
Object.defineProperty(exports, "__esModule", { value: true });
exports.TranslatableFieldsTab = void 0;
var react_1 = __importDefault(require("react"));
var Tab_1 = __importDefault(require("@mui/material/Tab"));
var ra_core_1 = require("ra-core");
var inflection_1 = require("inflection");
/**
 * Single tab that selects a locale in a TranslatableFields component.
 * @see TranslatableFields
 */
var TranslatableFieldsTab = function (props) {
    var locale = props.locale, _a = props.groupKey, groupKey = _a === void 0 ? '' : _a, rest = __rest(props, ["locale", "groupKey"]);
    var translate = (0, ra_core_1.useTranslate)();
    return (react_1.default.createElement(Tab_1.default, __assign({ id: "translatable-header-".concat(groupKey).concat(locale), label: translate("ra.locales.".concat(groupKey).concat(locale), {
            _: (0, inflection_1.capitalize)(locale),
        }) }, rest)));
};
exports.TranslatableFieldsTab = TranslatableFieldsTab;
//# sourceMappingURL=TranslatableFieldsTab.js.map