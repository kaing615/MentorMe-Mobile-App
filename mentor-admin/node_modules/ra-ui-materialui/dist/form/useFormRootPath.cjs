"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useFormRootPath = void 0;
var react_router_dom_1 = require("react-router-dom");
/**
 * This hook infers the tabbed form root path from the current location.
 */
var useFormRootPath = function () {
    var location = (0, react_router_dom_1.useLocation)();
    var createMatch = (0, react_router_dom_1.matchPath)(':resource/create/*', location.pathname);
    var editMatch = (0, react_router_dom_1.matchPath)(':resource/:id/*', location.pathname);
    if (createMatch) {
        return createMatch.pathnameBase;
    }
    if (editMatch) {
        return editMatch.pathnameBase;
    }
    return '';
};
exports.useFormRootPath = useFormRootPath;
//# sourceMappingURL=useFormRootPath.js.map