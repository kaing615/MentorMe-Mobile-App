import { useEffect, useRef } from 'react';
import { useLocation } from 'react-router';
/**
 * A hook to use inside the component passed to FallbackComponent
 * of react-error-boundary. It resets the error boundary state whenever
 * the location changes
 * @param {Function} resetErrorBoundary
 */
export var useResetErrorBoundaryOnLocationChange = function (resetErrorBoundary) {
    var pathname = useLocation().pathname;
    var originalPathname = useRef(pathname);
    useEffect(function () {
        if (pathname !== originalPathname.current) {
            resetErrorBoundary();
        }
    }, [pathname, resetErrorBoundary]);
};
//# sourceMappingURL=useResetErrorBoundaryOnLocationChange.js.map