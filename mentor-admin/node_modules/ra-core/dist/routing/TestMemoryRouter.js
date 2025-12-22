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
import * as React from 'react';
import { createMemoryRouter, RouterProvider, useLocation, useNavigate, } from 'react-router-dom';
var UseLocation = function (_a) {
    var locationCallback = _a.locationCallback;
    var location = useLocation();
    locationCallback(location);
    return null;
};
var UseNavigate = function (_a) {
    var navigateCallback = _a.navigateCallback;
    var navigate = useNavigate();
    navigateCallback(navigate);
    return null;
};
/**
 * Wrapper around react-router's `createMemoryRouter` to be used in test components.
 *
 * It is similar to `MemoryRouter` but it supports
 * [data APIs](https://reactrouter.com/en/main/routers/picking-a-router#data-apis).
 *
 * Additionally, it provides
 * - a `locationCallback` prop to get the location in the test
 * - a `navigateCallback` prop to be able to navigate in the test
 */
export var TestMemoryRouter = function (_a) {
    var children = _a.children, locationCallback = _a.locationCallback, navigateCallback = _a.navigateCallback, rest = __rest(_a, ["children", "locationCallback", "navigateCallback"]);
    var router = createMemoryRouter([
        {
            path: '*',
            element: (React.createElement(React.Fragment, null,
                children,
                locationCallback && (React.createElement(UseLocation, { locationCallback: locationCallback })),
                navigateCallback && (React.createElement(UseNavigate, { navigateCallback: navigateCallback })))),
        },
    ], __assign({ future: {
            v7_fetcherPersist: false,
            v7_normalizeFormMethod: false,
            v7_partialHydration: false,
            v7_relativeSplatPath: false,
            v7_skipActionErrorRevalidation: false,
        } }, rest));
    return (React.createElement(RouterProvider, { router: router, 
        // @ts-expect-error react-router types are not up-to-date
        future: { v7_startTransition: false, v7_relativeSplatPath: false } }));
};
//# sourceMappingURL=TestMemoryRouter.js.map