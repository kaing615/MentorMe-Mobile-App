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
Object.defineProperty(exports, "__esModule", { value: true });
exports.TranslatableInputsTabContentClasses = exports.TranslatableInputsTabContent = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
var styles_1 = require("@mui/material/styles");
var material_1 = require("@mui/material");
var clsx_1 = __importDefault(require("clsx"));
var ra_core_1 = require("ra-core");
/**
 * Default container for a group of translatable inputs inside a TranslatableInputs component.
 * @see TranslatableInputs
 */
var TranslatableInputsTabContent = function (inProps) {
    var _a;
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, _b = props.groupKey, groupKey = _b === void 0 ? '' : _b, locale = props.locale, other = __rest(props, ["children", "groupKey", "locale"]);
    var _c = (0, ra_core_1.useTranslatableContext)(), selectedLocale = _c.selectedLocale, getRecordForLocale = _c.getRecordForLocale;
    var parentSourceContext = (0, ra_core_1.useSourceContext)();
    var record = (0, ra_core_1.useRecordContext)(props);
    // The SourceContext will be read by children of TranslatableInputs to compute their composed source and label
    //
    // <TranslatableInputs locales={['en', 'fr']} /> => SourceContext is "fr"
    //     <TextInput source="description" /> => final source for this input will be "description.fr"
    // </TranslatableInputs>
    var sourceContext = (0, react_1.useMemo)(function () { return ({
        getSource: function (source) {
            if (!source) {
                throw new Error('Children of TranslatableInputs must have a source');
            }
            return parentSourceContext.getSource("".concat(source, ".").concat(locale));
        },
        getLabel: function (source) {
            return parentSourceContext.getLabel(source);
        },
    }); }, [locale, parentSourceContext]);
    // As fields rely on the RecordContext to get their values and have no knowledge of the locale,
    // we need to create a new record with the values for the current locale only
    // Given the record { title: { en: 'title_en', fr: 'title_fr' } } and the locale 'fr',
    // the record for the locale 'fr' will be { title: 'title_fr' }
    var recordForLocale = (0, react_1.useMemo)(function () { return getRecordForLocale(record, locale); }, [getRecordForLocale, record, locale]);
    return (React.createElement(ra_core_1.FormGroupContextProvider, { name: "".concat(groupKey).concat(locale) },
        React.createElement(Root, __assign({ role: "tabpanel", id: "translatable-content-".concat(groupKey).concat(locale), "aria-labelledby": "translatable-header-".concat(groupKey).concat(locale), className: (0, clsx_1.default)(exports.TranslatableInputsTabContentClasses.root, (_a = {},
                _a[exports.TranslatableInputsTabContentClasses.hidden] = selectedLocale !== locale,
                _a)) }, other),
            React.createElement(ra_core_1.SourceContextProvider, { value: sourceContext },
                React.createElement(ra_core_1.RecordContextProvider, { value: recordForLocale }, children)))));
};
exports.TranslatableInputsTabContent = TranslatableInputsTabContent;
var PREFIX = 'RaTranslatableInputsTabContent';
exports.TranslatableInputsTabContentClasses = {
    root: "".concat(PREFIX, "-root"),
    hidden: "".concat(PREFIX, "-hidden"),
};
var Root = (0, styles_1.styled)(material_1.Stack, { name: PREFIX })(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["&.".concat(exports.TranslatableInputsTabContentClasses.root)] = {
            flexGrow: 1,
            paddingLeft: theme.spacing(2),
            paddingRight: theme.spacing(2),
            paddingTop: theme.spacing(1),
            paddingBottom: theme.spacing(1),
            borderRadius: 0,
            borderBottomLeftRadius: theme.shape.borderRadius,
            borderBottomRightRadius: theme.shape.borderRadius,
            border: "1px solid ".concat((theme.vars || theme).palette.divider),
            borderTop: 0,
        },
        _b["&.".concat(exports.TranslatableInputsTabContentClasses.hidden)] = {
            display: 'none',
        },
        _b);
});
//# sourceMappingURL=TranslatableInputsTabContent.js.map