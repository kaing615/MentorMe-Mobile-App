import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
/**
 * Scroll the window to top when the target location contains the _scrollToTop state
 *
 * @see CoreAdminRouter where it's enabled by default
 *
 * @example // usage in buttons
 * import { Link } from 'react-router-dom';
 * import { Button } from '@mui/material';
 *
 * const FooButton = () => (
 *     <Button
 *         component={Link}
 *         to={{
 *             pathname: '/foo',
 *             state: { _scrollToTop: true },
 *         }}
 *     >
 *         Go to foo
 *     </Button>
 * );
 */
export var useScrollToTop = function () {
    var location = useLocation();
    useEffect(function () {
        var _a;
        if (((_a = location.state) === null || _a === void 0 ? void 0 : _a._scrollToTop) &&
            typeof window != 'undefined' &&
            typeof window.scrollTo === 'function') {
            window.scrollTo(0, 0);
        }
    }, [location]);
};
//# sourceMappingURL=useScrollToTop.js.map