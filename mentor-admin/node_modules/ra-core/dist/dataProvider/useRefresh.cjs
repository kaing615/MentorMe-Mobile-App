"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useRefresh = void 0;
var react_1 = require("react");
var react_query_1 = require("@tanstack/react-query");
/**
 * Hook for triggering a page refresh. Returns a callback function.
 *
 * The callback invalidates all queries and refetches the active ones.
 * Any component depending on react-query data will be re-rendered.
 *
 * @example
 *
 * const refresh = useRefresh();
 * const handleClick = () => {
 *     refresh();
 * };
 */
var useRefresh = function () {
    var queryClient = (0, react_query_1.useQueryClient)();
    return (0, react_1.useCallback)(function () {
        queryClient.invalidateQueries();
    }, [queryClient]);
};
exports.useRefresh = useRefresh;
//# sourceMappingURL=useRefresh.js.map