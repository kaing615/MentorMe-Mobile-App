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
Object.defineProperty(exports, "__esModule", { value: true });
exports.SimpleListEditor = void 0;
var React = __importStar(require("react"));
var material_1 = require("@mui/material");
var ra_core_1 = require("ra-core");
/**
 * A component which provides a configuration UI to tweak the SimpleList
 *
 * @param {SimpleListEditorProps} props
 * @param props.defaultPrimaryText The SimpleList columns
 * @param {String} props.resource The resource
 * @param {String} props.preferenceKey The key of the columns preferences
 */
var SimpleListEditor = function (props) {
    var _a = props.defaultPrimaryText, defaultPrimaryText = _a === void 0 ? '' : _a, _b = props.defaultSecondaryText, defaultSecondaryText = _b === void 0 ? '' : _b, _c = props.defaultTertiatyText, defaultTertiatyText = _c === void 0 ? '' : _c;
    (0, ra_core_1.useSetInspectorTitle)('ra.inspector.SimpleList.title', { _: 'List' });
    var translate = (0, ra_core_1.useTranslate)();
    var primaryTextField = (0, ra_core_1.usePreferenceInput)('primaryText', defaultPrimaryText);
    var secondaryTextField = (0, ra_core_1.usePreferenceInput)('secondaryText', defaultSecondaryText);
    var tertiaryTextField = (0, ra_core_1.usePreferenceInput)('tertiaryText', defaultTertiatyText);
    return (React.createElement("form", null,
        React.createElement(material_1.TextField, __assign({ label: translate('ra.configurable.SimpleList.primaryText', {
                _: 'Primary Text',
            }) }, primaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } })),
        React.createElement(material_1.TextField, __assign({ label: translate('ra.configurable.SimpleList.secondaryText', {
                _: 'Secondary Text',
            }) }, secondaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } })),
        React.createElement(material_1.TextField, __assign({ label: translate('ra.configurable.SimpleList.tertiaryText', {
                _: 'Tertiary Text',
            }) }, tertiaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } }))));
};
exports.SimpleListEditor = SimpleListEditor;
//# sourceMappingURL=SimpleListEditor.js.map