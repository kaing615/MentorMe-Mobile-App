"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useScrollToTop = void 0;
var react_1 = require("react");
var react_router_dom_1 = require("react-router-dom");
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
var useScrollToTop = function () {
    var location = (0, react_router_dom_1.useLocation)();
    (0, react_1.useEffect)(function () {
        var _a;
        if (((_a = location.state) === null || _a === void 0 ? void 0 : _a._scrollToTop) &&
            typeof window != 'undefined' &&
            typeof window.scrollTo === 'function') {
            window.scrollTo(0, 0);
        }
    }, [location]);
};
exports.useScrollToTop = useScrollToTop;
//# sourceMappingURL=useScrollToTop.js.map