"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useSplatPathBase = void 0;
var react_router_dom_1 = require("react-router-dom");
/**
 * Utility hook to get the base path of a splat path.
 * Compatible both with react-router v6 and v7.
 *
 * Example:
 * If a splat path is defined as `/posts/:id/show/*`,
 * and the current location is `/posts/12/show/3`,
 * this hook will return `/posts/12/show`.
 *
 * Solution inspired by
 * https://github.com/remix-run/react-router/issues/11052#issuecomment-1828470203
 */
var useSplatPathBase = function () {
    var location = (0, react_router_dom_1.useLocation)();
    var params = (0, react_router_dom_1.useParams)();
    var splatPathRelativePart = params['*'];
    var splatPathBase = location.pathname.replace(new RegExp("/".concat(splatPathRelativePart, "$")), '');
    return splatPathBase;
};
exports.useSplatPathBase = useSplatPathBase;
//# sourceMappingURL=useSplatPathBase.js.map