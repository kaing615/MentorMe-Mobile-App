import * as React from 'react';
/**
 * Creates a react-router Router unless the app is already inside existing router.
 * Also creates a BasenameContext with the basename prop
 */
export declare const AdminRouter: ({ basename, children }: AdminRouterProps) => React.JSX.Element;
export interface AdminRouterProps {
    basename?: string;
    children: React.ReactNode;
}
//# sourceMappingURL=AdminRouter.d.ts.map