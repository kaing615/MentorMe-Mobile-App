import { QueryObserverResult, UseQueryOptions } from '@tanstack/react-query';
/**
 * Hook for getting user permissions
 *
 * Calls the authProvider.getPermissions() method using react-query.
 * If the authProvider returns a rejected promise, returns empty permissions.
 *
 * The return value updates according to the request state:
 *
 * - start: { isPending: true }
 * - success: { permissions: [any], isPending: false }
 * - error: { error: [error from provider], isPending: false }
 *
 * Useful to enable features based on user permissions
 *
 * @param {Object} params Any params you want to pass to the authProvider
 *
 * @returns The current auth check state. Destructure as { permissions, error, isPending, refetch }.
 *
 * @example
 *     import { usePermissions } from 'react-admin';
 *
 *     const PostDetail = () => {
 *         const { isPending, permissions } = usePermissions();
 *         if (!isPending && permissions == 'editor') {
 *             return <PostEdit />
 *         } else {
 *             return <PostShow />
 *         }
 *     };
 */
declare const usePermissions: <PermissionsType = any, ErrorType = Error>(params?: {}, queryParams?: UsePermissionsOptions<PermissionsType, ErrorType>) => UsePermissionsResult<PermissionsType, ErrorType>;
export default usePermissions;
export interface UsePermissionsOptions<PermissionsType = any, ErrorType = Error> extends Omit<UseQueryOptions<PermissionsType, ErrorType>, 'queryKey' | 'queryFn'> {
    onSuccess?: (data: PermissionsType) => void;
    onError?: (err: ErrorType) => void;
    onSettled?: (data?: PermissionsType, error?: ErrorType | null) => void;
}
export type UsePermissionsResult<PermissionsType = any, ErrorType = Error> = QueryObserverResult<PermissionsType, ErrorType> & {
    permissions: PermissionsType | undefined;
};
//# sourceMappingURL=usePermissions.d.ts.map