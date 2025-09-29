import { useEffect, useState } from 'react';
import { useQueryClient, QueryObserver } from '@tanstack/react-query';
/**
 * Check if react-query has already fetched data for a query key.
 *
 * This hook is reactive.
 *
 * @example
 * const isCustomerLoaded = useIsDataLoaded(['customers', 'getOne', { id: customerId }]);
 *
 * @returns {boolean} true if the data is loaded, false otherwise
 */
export var useIsDataLoaded = function (queryKey, options) {
    if (options === void 0) { options = {}; }
    var _a = options.enabled, enabled = _a === void 0 ? true : _a;
    var queryClient = useQueryClient();
    var _b = useState(function () {
        if (!enabled) {
            return false;
        }
        return queryClient.getQueryData(queryKey) !== undefined;
    }), isDataLoaded = _b[0], setDataLoaded = _b[1];
    useEffect(function () {
        if (!enabled)
            return;
        if (queryClient.getQueryData(queryKey) === undefined) {
            var observer = new QueryObserver(queryClient, { queryKey: queryKey });
            var unsubscribe_1 = observer.subscribe(function (result) {
                setDataLoaded(!result.isPending);
                unsubscribe_1();
            });
            return unsubscribe_1;
        }
    }, [enabled, queryKey, queryClient]);
    return isDataLoaded;
};
//# sourceMappingURL=useIsDataLoaded.js.map