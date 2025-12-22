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
Object.defineProperty(exports, "__esModule", { value: true });
exports.TopToolbar = void 0;
var React = __importStar(require("react"));
var material_1 = require("@mui/material");
var styles_1 = require("@mui/material/styles");
var TopToolbar = function (inProps) {
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var isXSmall = (0, material_1.useMediaQuery)(function (theme) {
        return theme.breakpoints.down('sm');
    });
    return (React.createElement(StyledToolbar, __assign({ disableGutters: true, variant: isXSmall ? 'regular' : 'dense' }, sanitizeToolbarRestProps(props))));
};
exports.TopToolbar = TopToolbar;
exports.default = exports.TopToolbar;
var PREFIX = 'RaTopToolbar';
var StyledToolbar = (0, styles_1.styled)(material_1.Toolbar, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'flex-end',
            gap: theme.spacing(1),
            whiteSpace: 'nowrap',
            flex: '0 1 auto',
            padding: theme.spacing(0.5),
            paddingBottom: theme.spacing(1)
        },
        _b[theme.breakpoints.down('md')] = {
            flex: '0 1 100%',
        },
        _b[theme.breakpoints.down('sm')] = {
            backgroundColor: (theme.vars || theme).palette.background.paper,
            padding: 0,
            paddingBottom: 0,
        },
        _b);
});
var sanitizeToolbarRestProps = function (_a) {
    var hasCreate = _a.hasCreate, props = __rest(_a, ["hasCreate"]);
    return props;
};
//# sourceMappingURL=TopToolbar.js.map