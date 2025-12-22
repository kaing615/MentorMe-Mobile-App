import React from 'react';
import { notifyManager, useQueryClient } from '@tanstack/react-query';
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
export var useLoading = function () {
    var client = useQueryClient();
    var mountedRef = React.useRef(false);
    var isFetchingRef = React.useRef(client.isFetching() > 0);
    var isMutatingRef = React.useRef(client.isMutating() > 0);
    var _a = React.useState(isFetchingRef.current || isMutatingRef.current), isLoading = _a[0], setIsLoading = _a[1];
    React.useEffect(function () {
        mountedRef.current = true;
        var unsubscribeQueryCache = client.getQueryCache().subscribe(notifyManager.batchCalls(function () {
            if (mountedRef.current) {
                isFetchingRef.current = client.isFetching() > 0;
                setIsLoading(isFetchingRef.current || isMutatingRef.current);
            }
        }));
        var unsubscribeMutationCache = client.getMutationCache().subscribe(notifyManager.batchCalls(function () {
            if (mountedRef.current) {
                isMutatingRef.current = client.isMutating() > 0;
                setIsLoading(isFetchingRef.current || isMutatingRef.current);
            }
        }));
        return function () {
            mountedRef.current = false;
            unsubscribeQueryCache();
            unsubscribeMutationCache();
        };
    }, [client]);
    return isLoading;
};
//# sourceMappingURL=useLoading.js.map