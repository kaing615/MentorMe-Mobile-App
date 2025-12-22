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
import * as React from 'react';
import mediaQuery from 'css-mediaquery';
import { createTheme, ThemeProvider } from '@mui/material/styles';
function createMatchMedia(width) {
    return function (query) { return ({
        matches: mediaQuery.match(query, {
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
export var DeviceTestWrapper = function (_a) {
    var _b = _a.width, width = _b === void 0 ? 'md' : _b, children = _a.children;
    var theme = createTheme();
    // Use https://github.com/ericf/css-mediaquery as polyfill.
    var ssrMatchMedia = createMatchMedia(theme.breakpoints.values[width]);
    return (React.createElement(ThemeProvider, { theme: __assign(__assign({}, theme), { components: {
                MuiUseMediaQuery: {
                    defaultProps: {
                        ssrMatchMedia: ssrMatchMedia,
                        matchMedia: ssrMatchMedia,
                    },
                },
            } }) }, children));
};
//# sourceMappingURL=DeviceTestWrapper.js.map