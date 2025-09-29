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
export declare const useIsDataLoaded: (queryKey: any, options?: {
    enabled?: boolean;
}) => boolean;
//# sourceMappingURL=useIsDataLoaded.d.ts.map