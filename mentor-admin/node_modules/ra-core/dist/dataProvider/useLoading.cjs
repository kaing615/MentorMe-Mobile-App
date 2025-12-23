"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.useLoading = void 0;
var react_1 = __importDefault(require("react"));
var react_query_1 = require("@tanstack/react-query");
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
var useLoading = function () {
    var client = (0, react_query_1.useQueryClient)();
    var mountedRef = react_1.default.useRef(false);
    var isFetchingRef = react_1.default.useRef(client.isFetching() > 0);
    var isMutatingRef = react_1.default.useRef(client.isMutating() > 0);
    var _a = react_1.default.useState(isFetchingRef.current || isMutatingRef.current), isLoading = _a[0], setIsLoading = _a[1];
    react_1.default.useEffect(function () {
        mountedRef.current = true;
        var unsubscribeQueryCache = client.getQueryCache().subscribe(react_query_1.notifyManager.batchCalls(function () {
            if (mountedRef.current) {
                isFetchingRef.current = client.isFetching() > 0;
                setIsLoading(isFetchingRef.current || isMutatingRef.current);
            }
        }));
        var unsubscribeMutationCache = client.getMutationCache().subscribe(react_query_1.notifyManager.batchCalls(function () {
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
exports.useLoading = useLoading;
//# sourceMappingURL=useLoading.js.map