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
exports.LoadingClasses = exports.Loading = void 0;
var React = __importStar(require("react"));
var styles_1 = require("@mui/material/styles");
var material_1 = require("@mui/material");
var CircularProgress_1 = __importDefault(require("@mui/material/CircularProgress"));
var ra_core_1 = require("ra-core");
var Loading = function (inProps) {
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, _a = props.loadingPrimary, loadingPrimary = _a === void 0 ? 'ra.page.loading' : _a, _b = props.loadingSecondary, loadingSecondary = _b === void 0 ? 'ra.message.loading' : _b, _c = props.timeout, timeout = _c === void 0 ? 1000 : _c, rest = __rest(props, ["className", "loadingPrimary", "loadingSecondary", "timeout"]);
    var oneSecondHasPassed = (0, ra_core_1.useTimeout)(timeout);
    var translate = (0, ra_core_1.useTranslate)();
    return oneSecondHasPassed ? (React.createElement(Root, __assign({ className: className }, rest),
        React.createElement("div", { className: exports.LoadingClasses.message },
            React.createElement(CircularProgress_1.default, { className: exports.LoadingClasses.icon }),
            React.createElement(material_1.Typography, { variant: "h5", sx: {
                    mt: 3,
                    color: 'text.secondary',
                } }, translate(loadingPrimary, { _: loadingPrimary })),
            React.createElement(material_1.Typography, { variant: "body2" }, translate(loadingSecondary, { _: loadingSecondary }))))) : null;
};
exports.Loading = Loading;
var PREFIX = 'RaLoading';
exports.LoadingClasses = {
    root: "".concat(PREFIX, "-root"),
    icon: "".concat(PREFIX, "-icon"),
    message: "".concat(PREFIX, "-message"),
};
var Root = (0, styles_1.styled)('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100%'
        },
        _b["& .".concat(exports.LoadingClasses.message)] = {
            textAlign: 'center',
            fontFamily: 'Roboto, sans-serif',
            color: (theme.vars || theme).palette.text.disabled,
            paddingTop: '1em',
            paddingBottom: '1em',
        },
        _b["& .".concat(exports.LoadingClasses.icon)] = {
            width: '9em',
            height: '9em',
        },
        _b);
});
//# sourceMappingURL=Loading.js.map