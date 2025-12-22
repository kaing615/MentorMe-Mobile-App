import { QueryObserverResult, UseQueryOptions } from '@tanstack/react-query';
/**
 * Hook for getting the authentication status
 *
 * Calls the authProvider.checkAuth() method asynchronously.
 *
 * The return value updates according to the authProvider request state:
 *
 * - isPending: true just after mount, while the authProvider is being called. false once the authProvider has answered.
 * - authenticated: true while loading. then true or false depending on the authProvider response.
 *
 * To avoid rendering a component and force waiting for the authProvider response, use the useAuthState() hook
 * instead of the useAuthenticated() hook.
 *
 * You can render different content depending on the authenticated status.
 *
 * @see useAuthenticated()
 *
 * @param {Object} params Any params you want to pass to the authProvider
 *
 * @param {Boolean} logoutOnFailure: Optional. Whether the user should be logged out if the authProvider fails to authenticate them. False by default.
 *
 * @returns The current auth check state. Destructure as { authenticated, error, isPending }.
 *
 * @example
 * import { useAuthState, Loading } from 'react-admin';
 *
 * const MyPage = () => {
 *     const { isPending, authenticated } = useAuthState();
 *     if (isPending) {
 *         return <Loading />;
 *     }
 *     if (authenticated) {
 *        return <AuthenticatedContent />;
 *     }
 *     return <AnonymousContent />;
 * };
 */
declare const useAuthState: <ErrorType = Error>(params?: any, logoutOnFailure?: boolean, queryOptions?: UseAuthStateOptions<ErrorType>) => UseAuthStateResult<ErrorType>;
type UseAuthStateOptions<ErrorType = Error> = Omit<UseQueryOptions<boolean, ErrorType>, 'queryKey' | 'queryFn'> & {
    onSuccess?: (data: boolean) => void;
    onError?: (err: ErrorType) => void;
    onSettled?: (data?: boolean, error?: Error) => void;
};
export type UseAuthStateResult<ErrorType = Error> = QueryObserverResult<boolean, ErrorType> & {
    authenticated?: QueryObserverResult<boolean, ErrorType>['data'];
};
export default useAuthState;
//# sourceMappingURL=useAuthState.d.ts.map