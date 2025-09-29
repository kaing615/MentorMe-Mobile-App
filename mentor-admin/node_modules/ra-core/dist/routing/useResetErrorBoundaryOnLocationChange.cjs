"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useResetErrorBoundaryOnLocationChange = void 0;
var react_1 = require("react");
var react_router_1 = require("react-router");
/**
 * A hook to use inside the component passed to FallbackComponent
 * of react-error-boundary. It resets the error boundary state whenever
 * the location changes
 * @param {Function} resetErrorBoundary
 */
var useResetErrorBoundaryOnLocationChange = function (resetErrorBoundary) {
    var pathname = (0, react_router_1.useLocation)().pathname;
    var originalPathname = (0, react_1.useRef)(pathname);
    (0, react_1.useEffect)(function () {
        if (pathname !== originalPathname.current) {
            resetErrorBoundary();
        }
    }, [pathname, resetErrorBoundary]);
};
exports.useResetErrorBoundaryOnLocationChange = useResetErrorBoundaryOnLocationChange;
//# sourceMappingURL=useResetErrorBoundaryOnLocationChange.js.map