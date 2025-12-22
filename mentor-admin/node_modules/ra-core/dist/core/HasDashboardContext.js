import { createContext, useContext } from 'react';
export var HasDashboardContext = createContext(false);
export var HasDashboardContextProvider = HasDashboardContext.Provider;
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
export var useHasDashboard = function () { return useContext(HasDashboardContext); };
//# sourceMappingURL=HasDashboardContext.js.map