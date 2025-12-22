"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useIsDataLoaded = void 0;
var react_1 = require("react");
var react_query_1 = require("@tanstack/react-query");
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
var useIsDataLoaded = function (queryKey, options) {
    if (options === void 0) { options = {}; }
    var _a = options.enabled, enabled = _a === void 0 ? true : _a;
    var queryClient = (0, react_query_1.useQueryClient)();
    var _b = (0, react_1.useState)(function () {
        if (!enabled) {
            return false;
        }
        return queryClient.getQueryData(queryKey) !== undefined;
    }), isDataLoaded = _b[0], setDataLoaded = _b[1];
    (0, react_1.useEffect)(function () {
        if (!enabled)
            return;
        if (queryClient.getQueryData(queryKey) === undefined) {
            var observer = new react_query_1.QueryObserver(queryClient, { queryKey: queryKey });
            var unsubscribe_1 = observer.subscribe(function (result) {
                setDataLoaded(!result.isPending);
                unsubscribe_1();
            });
            return unsubscribe_1;
        }
    }, [enabled, queryKey, queryClient]);
    return isDataLoaded;
};
exports.useIsDataLoaded = useIsDataLoaded;
//# sourceMappingURL=useIsDataLoaded.js.map