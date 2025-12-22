"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useHasDashboard = exports.HasDashboardContextProvider = exports.HasDashboardContext = void 0;
var react_1 = require("react");
exports.HasDashboardContext = (0, react_1.createContext)(false);
exports.HasDashboardContextProvider = exports.HasDashboardContext.Provider;
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
var useHasDashboard = function () { return (0, react_1.useContext)(exports.HasDashboardContext); };
exports.useHasDashboard = useHasDashboard;
//# sourceMappingURL=HasDashboardContext.js.map