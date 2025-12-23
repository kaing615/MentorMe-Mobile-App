export declare const HasDashboardContext: import("react").Context<boolean>;
export declare const HasDashboardContextProvider: import("react").Provider<boolean>;
/**
 * Returns true if the app has a dashboard defined at the <Admin> level.
 *
 * @private
 * @example
 * import { useHasDashboard } from 'react-admin';
 *
 * const MyMenu = () => {
 *    const hasDashboard = useHasDashboard();
 *    return (
 *       <Menu>
 *          {hasDashboard && <DashboardMenuItem />}
 *          <MenuItemLink to="/posts" />
 *          <MenuItemLink to="/comments" />
 *       </Menu>
 *     );
 * }
 */
export declare const useHasDashboard: () => boolean;
//# sourceMappingURL=HasDashboardContext.d.ts.map