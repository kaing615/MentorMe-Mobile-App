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
exports.LabeledClasses = exports.Labeled = void 0;
var React = __importStar(require("react"));
var material_1 = require("@mui/material");
var styles_1 = require("@mui/material/styles");
var clsx_1 = __importDefault(require("clsx"));
var ra_core_1 = require("ra-core");
var system_1 = require("@mui/system");
/**
 * Wrap a field or an input with a label if necessary.
 *
 * The label is displayed if:
 * - the field or input has a label prop that is not false, or
 * - the field or input has a source prop
 *
 * @example
 * <Labeled>
 *     <FooComponent source="title" />
 * </Labeled>
 */
var Labeled = function (inProps) {
    var _a;
    var _b, _c;
    var props = (0, system_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, _d = props.className, className = _d === void 0 ? '' : _d, color = props.color, _e = props.component, component = _e === void 0 ? 'span' : _e, fullWidth = props.fullWidth, isRequired = props.isRequired, label = props.label, resource = props.resource, source = props.source, TypographyProps = props.TypographyProps, rest = __rest(props, ["children", "className", "color", "component", "fullWidth", "isRequired", "label", "resource", "source", "TypographyProps"]);
    return (React.createElement(Root
    // @ts-ignore https://github.com/mui/material-ui/issues/29875
    , __assign({ 
        // @ts-ignore https://github.com/mui/material-ui/issues/29875
        component: component, className: (0, clsx_1.default)(className, (_a = {},
            _a[exports.LabeledClasses.fullWidth] = fullWidth,
            _a)) }, rest),
        label !== false &&
            children.props.label !== false &&
            typeof children.type !== 'string' &&
            // @ts-ignore
            ((_b = children.type) === null || _b === void 0 ? void 0 : _b.displayName) !== 'Labeled' &&
            // @ts-ignore
            ((_c = children.type) === null || _c === void 0 ? void 0 : _c.displayName) !== 'Labeled' ? (React.createElement(material_1.Typography, __assign({ sx: color
                ? { color: color }
                : {
                    color: function (theme) {
                        return (theme.vars || theme).palette.text
                            .secondary;
                    },
                }, className: exports.LabeledClasses.label }, TypographyProps),
            React.createElement(ra_core_1.FieldTitle, { label: label || children.props.label, source: source || children.props.source, resource: resource, isRequired: isRequired }))) : null,
        children));
};
exports.Labeled = Labeled;
exports.Labeled.displayName = 'Labeled';
var PREFIX = 'RaLabeled';
exports.LabeledClasses = {
    label: "".concat(PREFIX, "-label"),
    fullWidth: "".concat(PREFIX, "-fullWidth"),
};
var Root = (0, styles_1.styled)(material_1.Stack, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {
        display: 'inline-flex',
        marginBottom: '0.2em'
    },
    _a["&.".concat(exports.LabeledClasses.fullWidth)] = {
        width: '100%',
    },
    _a["& .".concat(exports.LabeledClasses.label)] = {
        fontSize: '0.75em',
        marginBottom: '0.2em',
    },
    _a));
//# sourceMappingURL=Labeled.js.map