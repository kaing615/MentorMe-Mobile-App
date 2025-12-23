import { UseQueryOptions } from '@tanstack/react-query';
/**
 * Restrict access to authenticated users.
 * Redirect anonymous users to the login page.
 *
 * Use it in your custom page components to require
 * authentication.
 *
 * You can set additional `authParams` at will if your authProvider
 * requires it.
 *
 * @example
 * import { Admin, CustomRoutes, useAuthenticated } from 'react-admin';
 *
 * const FooPage = () => {
 *     const { isPending } = useAuthenticated();
 *     if (isPending) return null;
 *     return <Foo />;
 * }
 *
 * const customRoutes = [
 *     <Route path="/foo" element={<FooPage />} />
 * ];
 *
 * const App = () => (
 *     <Admin>
 *         <CustomRoutes>{customRoutes}</CustomRoutes>
 *     </Admin>
 * );
 */
export declare const useAuthenticated: <ParamsType = any>({ params, logoutOnFailure, ...options }?: UseAuthenticatedOptions<ParamsType>) => import("./useAuthState").UseAuthStateResult<any>;
export type UseAuthenticatedOptions<ParamsType> = Omit<UseQueryOptions<boolean, any> & {
    params?: ParamsType;
}, 'queryKey' | 'queryFn'> & {
    logoutOnFailure?: boolean;
};
//# sourceMappingURL=useAuthenticated.d.ts.map