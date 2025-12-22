/**
 * A hook that returns the first resource for which users have access to the list page.
 * It calls the `authProvider.canAccess` if available to check the permissions.
 */
export declare const useFirstResourceWithListAccess: () => {
    resource: string | undefined;
    isPending: boolean;
};
//# sourceMappingURL=useFirstResourceWithListAccess.d.ts.map