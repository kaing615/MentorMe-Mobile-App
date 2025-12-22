import * as React from 'react';
import { ReactNode } from 'react';
/**
 * Restrict access to children to authenticated users.
 * Redirects anonymous users to the login page.
 *
 * Use it to decorate your custom page components to require
 * authentication.
 *
 * @see useAuthState
 *
 * @example
 * import { Admin, CustomRoutes, Authenticated } from 'react-admin';
 *
 * const customRoutes = [
 *     <Route
 *         path="/foo"
 *         element={
 *             <Authenticated authParams={{ foo: 'bar' }}>
 *                 <Foo />
 *             </Authenticated>
 *         }
 *     />
 * ];
 * const App = () => (
 *     <Admin>
 *         <CustomRoutes>{customRoutes}</CustomRoutes>
 *     </Admin>
 * );
 */
export declare const Authenticated: (props: AuthenticatedProps) => string | number | boolean | Iterable<React.ReactNode> | React.JSX.Element | null;
export interface AuthenticatedProps {
    children: ReactNode;
    authParams?: object;
    loading?: ReactNode;
    /**
     * @deprecated Authenticated now never renders children when not authenticated.
     */
    requireAuth?: boolean;
}
//# sourceMappingURL=Authenticated.d.ts.map