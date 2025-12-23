import * as React from 'react';
import { ComponentType } from 'react';
import { CoreAdminUIProps } from 'ra-core';
export declare const AdminUI: ({ accessDenied, authCallbackPage, authenticationError, catchAll, error, layout, loading, loginPage, notification, ...props }: AdminUIProps) => React.JSX.Element;
export interface AdminUIProps extends CoreAdminUIProps {
    /**
     * The component used to display notifications
     *
     * @see https://marmelab.com/react-admin/Admin.html#notification
     * @example
     * import { Admin, Notification } from 'react-admin';
     * import { dataProvider } from './dataProvider';
     *
     * const MyNotification = () => <Notification autoHideDuration={5000} />;
     *
     * const App = () => (
     *     <Admin notification={MyNotification} dataProvider={dataProvider}>
     *         ...
     *     </Admin>
     * );
     */
    notification?: ComponentType;
}
//# sourceMappingURL=AdminUI.d.ts.map