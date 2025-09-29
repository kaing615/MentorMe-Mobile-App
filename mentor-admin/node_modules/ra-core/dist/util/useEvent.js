import * as React from 'react';
import { useCallback } from 'react';
// allow the hook to work in SSR
var useLayoutEffect = typeof window !== 'undefined' ? React.useLayoutEffect : React.useEffect;
/**
 * Alternative to useCallback that doesn't update the callback when dependencies change
 *
 * @see https://reactjs.org/docs/hooks-faq.html#how-to-read-an-often-changing-value-from-usecallback
 * @see https://github.com/facebook/react/issues/14099#issuecomment-440013892
 */
export var useEvent = function (fn) {
    var ref = React.useRef(function () {
        throw new Error('Cannot call an event handler while rendering.');
    });
    useLayoutEffect(function () {
        ref.current = fn;
    });
    return useCallback(function () {
        var args = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args[_i] = arguments[_i];
        }
        return ref.current.apply(ref, args);
    }, []);
};
//# sourceMappingURL=useEvent.js.map