/**
 * Get the state of the dataProvider connection.
 *
 * Returns true if a query or a mutation is pending.
 *
 * Custom implementation because react-query's useIsFetching and useIsMutating
 * render each time the number of active queries changes, which is too often.
 *
 * @see useIsFetching
 * @see useIsMutating
 */
export declare const useLoading: () => boolean;
//# sourceMappingURL=useLoading.d.ts.map