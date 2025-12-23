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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.DeviceTestWrapper = void 0;
var React = __importStar(require("react"));
var css_mediaquery_1 = __importDefault(require("css-mediaquery"));
var styles_1 = require("@mui/material/styles");
function createMatchMedia(width) {
    return function (query) { return ({
        matches: css_mediaquery_1.default.match(query, {
            width: width,
        }),
        addListener: function () { },
        removeListener: function () { },
    }); };
}
/**
 * Test utility to simulate a device form factor for server-side mediaQueries
 *
 * Do not use inside a browser.
 *
 * @example
 *
 * <DeviceTestWrapper width="sm">
 *     <MyResponsiveComponent />
 * <DeviceTestWrapper>
 */
var DeviceTestWrapper = function (_a) {
    var _b = _a.width, width = _b === void 0 ? 'md' : _b, children = _a.children;
    var theme = (0, styles_1.createTheme)();
    // Use https://github.com/ericf/css-mediaquery as polyfill.
    var ssrMatchMedia = createMatchMedia(theme.breakpoints.values[width]);
    return (React.createElement(styles_1.ThemeProvider, { theme: __assign(__assign({}, theme), { components: {
                MuiUseMediaQuery: {
                    defaultProps: {
                        ssrMatchMedia: ssrMatchMedia,
                        matchMedia: ssrMatchMedia,
                    },
                },
            } }) }, children));
};
exports.DeviceTestWrapper = DeviceTestWrapper;
//# sourceMappingURL=DeviceTestWrapper.js.map