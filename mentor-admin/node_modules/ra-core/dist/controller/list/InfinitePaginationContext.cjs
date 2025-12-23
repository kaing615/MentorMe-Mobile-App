"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.InfinitePaginationContext = void 0;
var react_1 = require("react");
/**
 * Context to store the pagination callbacks from the useInfiniteListController() result.
 *
 * Use the useInfinitePaginationContext() hook to read the pagination callbacks.
 *
 * @typedef {Object} InfinitePaginationContextValue
 * @prop {Function} fetchNextPage a callback to fetch the next page
 * @prop {Function} fetchPreviousPage a callback to fetch the previous page

 * @example
 *
 * import { useListController, ListPaginationContext } from 'ra-core';
 *
 * const List = props => {
 *     const { fetchNextPage, fetchPreviousPage } = useInfiniteListController(props);
 *     return (
 *         <InfinitePaginationContext.Provider value={{ fetchNextPage, fetchPreviousPage }}>
 *             ...
 *         </InfinitePaginationContext.Provider>
 *     );
 * };
 */
exports.InfinitePaginationContext = (0, react_1.createContext)({
    hasNextPage: false,
    fetchNextPage: function () { return Promise.reject('not implemented'); },
    isFetchingNextPage: false,
    hasPreviousPage: false,
    fetchPreviousPage: function () { return Promise.reject('not implemented'); },
    isFetchingPreviousPage: false,
});
exports.InfinitePaginationContext.displayName = 'InfinitePaginationContext';
//# sourceMappingURL=InfinitePaginationContext.js.map